/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.other;

public class Snapper {
    private final Container container;
    private Element snappedTo;
    private Direction mainDir;
    private int mainPos;
    private boolean secondary;
    private int secondaryPos;

    public Snapper(Container container) {
        this.container = container;
    }

    public void move(Element element, int deltaX, int deltaY) {
        if (this.container.getSnappingRange() == 0) {
            element.move(deltaX, deltaY);
            return;
        }
        if (this.snappedTo == null) {
            this.moveUnsnapped(element, deltaX, deltaY);
        } else {
            this.moveSnapped(element, deltaX, deltaY);
        }
    }

    public void unsnap() {
        this.snappedTo = null;
    }

    private void moveUnsnapped(Element element, int deltaX, int deltaY) {
        element.move(deltaX, deltaY);
        if (deltaX > 0) {
            Element closest = null;
            int closestDist = Integer.MAX_VALUE;
            for (Element e : this.container.getElements()) {
                int dist;
                if (this.container.shouldNotSnapTo(e) || (dist = e.getX() - element.getX2()) <= 0 || dist > this.container.getSnappingRange() || closest != null && dist >= closestDist || !this.isNextToHorizontally(element, e)) continue;
                closest = e;
                closestDist = dist;
            }
            if (closest != null) {
                element.setPos(closest.getX() - element.getWidth(), element.getY());
                this.snapMain(closest, Direction.Right);
            }
        } else if (deltaX < 0) {
            Element closest = null;
            int closestDist = Integer.MAX_VALUE;
            for (Element e : this.container.getElements()) {
                int dist;
                if (this.container.shouldNotSnapTo(e) || (dist = element.getX() - e.getX2()) <= 0 || dist > this.container.getSnappingRange() || closest != null && dist >= closestDist || !this.isNextToHorizontally(element, e)) continue;
                closest = e;
                closestDist = dist;
            }
            if (closest != null) {
                element.setPos(closest.getX2(), element.getY());
                this.snapMain(closest, Direction.Left);
            }
        } else if (deltaY > 0) {
            Element closest = null;
            int closestDist = Integer.MAX_VALUE;
            for (Element e : this.container.getElements()) {
                int dist;
                if (this.container.shouldNotSnapTo(e) || (dist = e.getY() - element.getY2()) <= 0 || dist > this.container.getSnappingRange() || closest != null && dist >= closestDist || !this.isNextToVertically(element, e)) continue;
                closest = e;
                closestDist = dist;
            }
            if (closest != null) {
                element.setPos(element.getX(), closest.getY() - element.getHeight());
                this.snapMain(closest, Direction.Top);
            }
        } else if (deltaY < 0) {
            Element closest = null;
            int closestDist = Integer.MAX_VALUE;
            for (Element e : this.container.getElements()) {
                int dist;
                if (this.container.shouldNotSnapTo(e) || (dist = element.getY() - e.getY2()) <= 0 || dist > this.container.getSnappingRange() || closest != null && dist >= closestDist || !this.isNextToVertically(element, e)) continue;
                closest = e;
                closestDist = dist;
            }
            if (closest != null) {
                element.setPos(element.getX(), closest.getY2());
                this.snapMain(closest, Direction.Bottom);
            }
        }
    }

    private void moveSnapped(Element element, int deltaX, int deltaY) {
        switch (this.mainDir.ordinal()) {
            case 0: 
            case 1: {
                int dist;
                if (this.secondary) {
                    this.secondaryPos += deltaY;
                } else {
                    element.move(0, deltaY);
                }
                this.mainPos += deltaX;
                if (!this.isNextToHorizontally(element, this.snappedTo)) {
                    this.unsnap();
                    break;
                }
                if (this.secondary) break;
                if (deltaY > 0) {
                    int dist2 = this.snappedTo.getY2() - element.getY2();
                    if (dist2 <= 0 || dist2 >= this.container.getSnappingRange()) break;
                    element.setPos(element.getX(), this.snappedTo.getY2() - element.getHeight());
                    this.snapSecondary();
                    break;
                }
                if (deltaY >= 0 || (dist = this.snappedTo.getY() - element.getY()) >= 0 || dist <= -this.container.getSnappingRange()) break;
                element.setPos(element.getX(), this.snappedTo.getY());
                this.snapSecondary();
                break;
            }
            case 2: 
            case 3: {
                int dist;
                if (this.secondary) {
                    this.secondaryPos += deltaX;
                } else {
                    element.move(deltaX, 0);
                }
                this.mainPos += deltaY;
                if (!this.isNextToVertically(element, this.snappedTo)) {
                    this.unsnap();
                    break;
                }
                if (this.secondary) break;
                if (deltaX > 0) {
                    int dist3 = this.snappedTo.getX2() - element.getX2();
                    if (dist3 <= 0 || dist3 >= this.container.getSnappingRange()) break;
                    element.setPos(this.snappedTo.getX2() - element.getWidth(), element.getY());
                    this.snapSecondary();
                    break;
                }
                if (deltaX >= 0 || (dist = element.getX() - this.snappedTo.getX()) <= 0 || dist >= this.container.getSnappingRange()) break;
                element.setPos(this.snappedTo.getX(), element.getY());
                this.snapSecondary();
            }
        }
        if (Math.abs(this.mainPos) > this.container.getSnappingRange() * 5) {
            this.unsnap();
        } else if (Math.abs(this.secondaryPos) > this.container.getSnappingRange() * 5) {
            this.secondary = false;
        }
    }

    private void snapMain(Element element, Direction dir) {
        this.snappedTo = element;
        this.mainDir = dir;
        this.mainPos = 0;
        this.secondary = false;
    }

    private void snapSecondary() {
        this.secondary = true;
        this.secondaryPos = 0;
    }

    private boolean isBetween(int value, int min, int max) {
        return value > min && value < max;
    }

    private boolean isNextToHorizontally(Element e1, Element e2) {
        int h2;
        int y1 = e1.getY();
        int h1 = e1.getHeight();
        int y2 = e2.getY();
        return this.isBetween(y1, y2, y2 + (h2 = e2.getHeight())) || this.isBetween(y1 + h1, y2, y2 + h2) || this.isBetween(y2, y1, y1 + h1) || this.isBetween(y2 + h2, y1, y1 + h1);
    }

    private boolean isNextToVertically(Element e1, Element e2) {
        int w2;
        int x1 = e1.getX();
        int w1 = e1.getWidth();
        int x2 = e2.getX();
        return this.isBetween(x1, x2, x2 + (w2 = e2.getWidth())) || this.isBetween(x1 + w1, x2, x2 + w2) || this.isBetween(x2, x1, x1 + w1) || this.isBetween(x2 + w2, x1, x1 + w1);
    }

    public static interface Container {
        public Iterable<Element> getElements();

        public boolean shouldNotSnapTo(Element var1);

        public int getSnappingRange();
    }

    public static interface Element {
        public int getX();

        public int getY();

        default public int getX2() {
            return this.getX() + this.getWidth();
        }

        default public int getY2() {
            return this.getY() + this.getHeight();
        }

        public int getWidth();

        public int getHeight();

        public void setPos(int var1, int var2);

        public void move(int var1, int var2);
    }

    private static enum Direction {
        Right,
        Left,
        Top,
        Bottom;

    }
}


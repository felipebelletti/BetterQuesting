package betterquesting.api2.client.gui.misc;

import org.joml.Vector4f;

/**
 * Provides pre-made anchor points for GUIs with functions to quickly create new ones
 */
public class GuiAlign {
    public static final Vector4f FULL_BOX = new ImmutableVec4f(0F, 0F, 1F, 1F);

    public static final Vector4f TOP_LEFT = new ImmutableVec4f(0F, 0F, 0F, 0F);
    public static final Vector4f TOP_CENTER = new ImmutableVec4f(0.5F, 0F, 0.5F, 0F);
    public static final Vector4f TOP_RIGHT = new ImmutableVec4f(1F, 0F, 1F, 0F);
    public static final Vector4f TOP_EDGE = new ImmutableVec4f(0F, 0F, 1F, 0F);

    public static final Vector4f MID_LEFT = new ImmutableVec4f(0F, 0.5F, 0F, 0.5F);
    public static final Vector4f MID_CENTER = new ImmutableVec4f(0.5F, 0.5F, 0.5F, 0.5F);
    public static final Vector4f MID_RIGHT = new ImmutableVec4f(1F, 0.5F, 1F, 0.5F);

    public static final Vector4f BOTTOM_LEFT = new ImmutableVec4f(0F, 1F, 0F, 1F);
    public static final Vector4f BOTTOM_CENTER = new ImmutableVec4f(0.5F, 1F, 0.5F, 1F);
    public static final Vector4f BOTTOM_RIGHT = new ImmutableVec4f(1F, 1F, 1F, 1F);
    public static final Vector4f BOTTOM_EDGE = new ImmutableVec4f(0F, 1F, 1F, 1F);

    public static final Vector4f HALF_LEFT = new ImmutableVec4f(0F, 0F, 0.5F, 1F);
    public static final Vector4f HALF_RIGHT = new ImmutableVec4f(0.5F, 0F, 1F, 1F);
    public static final Vector4f HALF_TOP = new ImmutableVec4f(0F, 0F, 1F, 0.5F);
    public static final Vector4f HALF_BOTTOM = new ImmutableVec4f(0F, 0.5F, 1F, 1F);

    public static final Vector4f LEFT_EDGE = new ImmutableVec4f(0F, 0F, 0F, 1F);
    public static final Vector4f RIGHT_EDGE = new ImmutableVec4f(1F, 0F, 1F, 1F);

    /**
     * Takes two readable Vector4f points and merges them in a single Vector4f anchor region
     */
    public static Vector4f quickAnchor(Vector4f v1, Vector4f v2) {
        float x1 = Math.min(v1.x, v2.x);
        float y1 = Math.min(v1.y, v2.y);
        float x2 = Math.max(v1.z, v2.z);
        float y2 = Math.max(v1.w, v2.w);

        return new Vector4f(x1, y1, x2, y2);
    }

    private static class ImmutableVec4f extends Vector4f {
        public ImmutableVec4f(float x, float y, float z, float w) {
            super(x, y, z, w);
        }

        @Override
        public Vector4f set(float x, float y, float z, float w) {
            throw new UnsupportedOperationException("This vector is immutable");
        }

        @Override
        public Vector4f set(Vector4f v) {
            throw new UnsupportedOperationException("This vector is immutable");
        }
    }
}

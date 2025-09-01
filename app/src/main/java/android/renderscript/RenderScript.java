package android.renderscript;

/**
 * Stub implementation of RenderScript to allow compilation without the full Android SDK.
 * This is a minimal implementation that provides just enough to satisfy the compiler.
 */
public class RenderScript {
    public static class RSMessageHandler {
        public void run() {
            // No-op
        }
    }

    public static class BaseObj {
        protected BaseObj() {}
        public void destroy() {}
    }

    public static class Element extends BaseObj {
        public static class Builder {
            public Builder(RenderScript rs) {}
            public void add(Element element, String name) {}
            public Element create() { return new Element(); }
        }

        public static Element I8(RenderScript rs) { return new Element(); }
        public static Element I16(RenderScript rs) { return new Element(); }
        public static Element I32(RenderScript rs) { return new Element(); }
        public static Element U8(RenderScript rs) { return new Element(); }
        public static Element U16(RenderScript rs) { return new Element(); }
        public static Element U32(RenderScript rs) { return new Element(); }
        public static Element F32(RenderScript rs) { return new Element(); }
        public static Element F64(RenderScript rs) { return new Element(); }
        public static Element BOOLEAN(RenderScript rs) { return new Element(); }
    }

    public static class Type extends BaseObj {
        public static class Builder {
            public Builder(RenderScript rs, Element e) {}
            public Builder setX(int x) { return this; }
            public Builder setY(int y) { return this; }
            public Builder setZ(int z) { return this; }
            public Type create() { return new Type(); }
        }
    }

    public static class Allocation extends BaseObj {
        public static final int USAGE_SCRIPT = 1;
        public static final int USAGE_IO_INPUT = 2;
        public static final int USAGE_IO_OUTPUT = 4;
        public static final int USAGE_IO_MASK = 7;

        public static Allocation createTyped(RenderScript rs, Type type) { return new Allocation(); }
        public static Allocation createTyped(RenderScript rs, Type type, int usage) { return new Allocation(); }
        public void copyTo(byte[] d) {}
        public void copyFrom(byte[] d) {}
        public void copy1DRangeFrom(int off, int count, Object array) {}
        public void copy1DRangeTo(int off, int count, Object array) {}
        public void copy2DRangeFrom(int xoff, int yoff, int w, int h, Object array) {}
        public void copy2DRangeTo(int xoff, int yoff, int w, int h, Object array) {}
        public void syncAll(int location) {}
    }

    public static class Script extends BaseObj {
        public static class Builder {
            public Builder(RenderScript rs) {}
            public Script create() { return new Script(); }
        }

        public static class FieldBase {
            protected FieldBase() {}
            protected void init(RenderScript rs, int dimx, int usages) {}
            protected FieldBase(Element e) {}
        }

        public static class FieldPacker {
            public FieldPacker(int len) {}
            public void addF32(float v) {}
            public void addF32(Float2 v) {}
            public void addF32(Float3 v) {}
            public void addF32(Float4 v) {}
            public void addI32(int v) {}
            public void addI32(Int2 v) {}
            public void addI32(Int3 v) {}
            public void addI32(Int4 v) {}
            public void addU32(int v) {}
            public void addU32(Int2 v) {}
            public void addU32(Int3 v) {}
            public void addU32(Int4 v) {}
            public void addI16(short v) {}
            public void addI16(Short2 v) {}
            public void addI16(Short3 v) {}
            public void addI16(Short4 v) {}
            public void addU16(int v) {}
            public void addU16(Int2 v) {}
            public void addU16(Int3 v) {}
            public void addU16(Int4 v) {}
            public void addI8(byte v) {}
            public void addI8(Byte2 v) {}
            public void addI8(Byte3 v) {}
            public void addI8(Byte4 v) {}
            public void addU8(short v) {}
            public void addU8(Short2 v) {}
            public void addU8(Short3 v) {}
            public void addU8(Short4 v) {}
            public void addF64(double v) {}
            public void addF64(Double2 v) {}
            public void addF64(Double3 v) {}
            public void addF64(Double4 v) {}
            public void addObj(BaseObj obj) {}
            public byte[] getData() { return new byte[0]; }
        }

        protected void invoke(int slot) {}
        protected void invoke(int slot, FieldPacker v) {}
        protected void forEach(int slot, Allocation ain, Allocation aout, FieldPacker v) {}
        protected void forEach(int slot, Allocation ain, Allocation aout) {}
        protected void forEach(int slot, Allocation ain) {}
    }

    public static class ScriptC extends Script {
        public ScriptC(RenderScript rs, Resources resources, int id) {}
    }

    public static class Float2 {
        public float x, y;
    }

    public static class Float3 {
        public float x, y, z;
    }

    public static class Float4 {
        public float x, y, z, w;
    }

    public static class Int2 {
        public int x, y;
    }

    public static class Int3 {
        public int x, y, z;
    }

    public static class Int4 {
        public int x, y, z, w;
    }

    public static class Short2 {
        public short x, y;
    }

    public static class Short3 {
        public short x, y, z;
    }

    public static class Short4 {
        public short x, y, z, w;
    }

    public static class Byte2 {
        public byte x, y;
    }

    public static class Byte3 {
        public byte x, y, z;
    }

    public static class Byte4 {
        public byte x, y, z, w;
    }

    public static class Double2 {
        public double x, y;
    }

    public static class Double3 {
        public double x, y, z;
    }

    public static class Double4 {
        public double x, y, z, w;
    }

    public static RenderScript create(Context context) {
        return new RenderScript();
    }

    public void setMessageHandler(RSMessageHandler msg) {}
    public void finish() {}
    public void destroy() {}
    public static void releaseAllContexts() {}
    public void sendMessage(int id, int[] data) {}
}

// Stub Context class
class Context {}

// Stub Resources class
class Resources {}

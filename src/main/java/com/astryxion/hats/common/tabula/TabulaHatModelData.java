package com.astryxion.hats.common.tabula;

import java.util.ArrayList;
import java.util.List;

public class TabulaHatModelData {

    public int textureWidth = 64;
    public int textureHeight = 64;
    public final List<Bone> roots = new ArrayList<>();

    public static class Bone {
        public String name = "";
        public float pivotX;
        public float pivotY;
        public float pivotZ;
        public float rotX;
        public float rotY;
        public float rotZ;
        public boolean mirror;
        public boolean visible = true;
        public int texW;
        public int texH;
        public final List<Cube> cubes = new ArrayList<>();
        public final List<Bone> children = new ArrayList<>();
    }

    public static class Cube {
        public float originX;
        public float originY;
        public float originZ;
        public float sizeX;
        public float sizeY;
        public float sizeZ;
        public float texU;
        public float texV;
        public float inflateX;
        public float inflateY;
        public float inflateZ;
        public boolean mirror;
    }
}

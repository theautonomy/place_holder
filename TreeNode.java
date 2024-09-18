package com.deblauwe.examples.shoelacethymeleafalpine;

import java.util.List;

public class TreeNode {
    private String id;
    private String name;
    private boolean hasChildren;
    private List<TreeNode> children;

    public TreeNode(String id, String name, boolean hasChildren) {
        this.id = id;
        this.name = name;
        this.hasChildren = hasChildren;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isHasChildren() {
        return hasChildren;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode> children) {
        this.children = children;
    }
}

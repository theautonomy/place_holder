package com.deblauwe.examples.shoelacethymeleafalpine;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.ArrayList;

@Controller
public class TreeController {

    @GetMapping("/sample")
    public String sample() {
        return "sample";
    }

    @GetMapping("/sample2")
    public String sample2() {
        return "sample2";
    }

    @GetMapping("/sample3")
    public String sample3() {
        return "sample3";
    }

    @GetMapping("/sample4")
    public String sample4() {
        return "sample4";
    }

    @GetMapping("/tree")
    public String index() {
        return "tree";
    }

    @GetMapping("/nodes/{parentId}")
    public String getTreeNodes(@PathVariable String parentId, Model model) {
        List<TreeNode> nodes = getNodesForParentId(parentId);
        model.addAttribute("nodes", nodes);
        return "nodes";
    }

    private List<TreeNode> getNodesForParentId(String parentId) {
        List<TreeNode> nodes = new ArrayList<>();
        switch (parentId) {
            case "root":
                nodes.add(new TreeNode("1", "Node 1", true));
                nodes.add(new TreeNode("2", "Node 2", true));
                break;
            case "1":
                nodes.add(new TreeNode("1-1", "Node 1-1", true));
                nodes.add(new TreeNode("1-2", "Node 1-2", false));
                break;
            case "1-1":
                nodes.add(new TreeNode("1-1-1", "Node 1-1-1", false));
                break;
            case "2":
                nodes.add(new TreeNode("2-1", "Node 2-1", true));
                break;
            case "2-1":
                nodes.add(new TreeNode("2-1-1", "Node 2-1-1", true));
                break;
            case "2-1-1":
                nodes.add(new TreeNode("2-1-1-1", "Node 2-1-1-1", false));
                nodes.add(new TreeNode("2-1-1-2", "Node 2-1-1-2", false));
                break;
        }
        return nodes;
    }
}

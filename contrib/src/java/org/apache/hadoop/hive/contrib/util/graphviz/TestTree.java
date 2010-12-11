package org.apache.hadoop.hive.contrib.util.graphviz;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: fix the switching between using private members and the
 * getter and setter functions
 * TestTree.
 *
 */

public class TestTree {
  private final List<TestTree> children;
  private final String name;
  private int uniqueID;

  static final String complexExampleTree = "(TOK_QUERY (TOK_FROM (TOK_TABREF src)) (TOK_INSERT (TOK_DESTINATION (TOK_DIR TOK_TMP_FILE)) (TOK_SELECT (TOK_SELEXPR (TOK_FUNCTION lower 'AbC 123')) (TOK_SELEXPR (TOK_FUNCTION upper 'AbC 123'))) (TOK_WHERE (= (TOK_TABLE_OR_COL key) 86))))";
  static final String LEAF_OPTIONS = "[color=lightblue, style=filled]";

  public TestTree(String inputName) {
    name = inputName;
    children = new ArrayList<TestTree>();
    uniqueID = 0;
  }

  public List<TestTree> getChildren() {
    return children;
  }

  public void addChildren(List<TestTree> newChildren) {
    children.addAll(newChildren);
  }

  public void addChild(TestTree child) {
    children.add(child);
  }

  public int getID() {
    return uniqueID;
  }

  public void setID(int uniqueID) {
    this.uniqueID = uniqueID;
  }

/**
 * give each node a new uniqueID
 * @param rootID id for the root node
 * @return highestID of a child node
 */
  public int renumber(int rootID) {
    setID(rootID);
    for (TestTree child : children) {
      rootID += 1;
      rootID = child.renumber(rootID);
    }
    return rootID;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("(" + name + "[" + getID() + "] ");
    for (TestTree t : children) {
      result.append(t.toString());
      result.append(", ");
    }
    result.append(" )");
    return result.toString();
  }

  /**
   * return the list of lines necessary to go in a graphviz
   * file.  Approach is to map out the left
   * @return
   */
  public List<String> toGraphviz() {
    // leaves on the tree
    List<String> outputLines = new ArrayList<String>();
    outputLines.add(getID() + " [label=" + name + "]" );
    if (children.size() == 0) {
      outputLines.add(getID() + LEAF_OPTIONS);
    }
    for (TestTree child : children) {
      outputLines.add(getID() + " -> " + child.getID());
      outputLines.addAll(child.toGraphviz());
    }

    return outputLines;
  }

  public void outputGraphviz(String filename) {
    try {
      FileWriter file = new FileWriter(filename);

      file.write("digraph TestTree {\n");

      this.renumber(0);
      List<String> fileContents = this.toGraphviz();
      for (String line : fileContents) {
        file.write(line);
        file.write("\n");
      }

      file.write("}\n");
      file.close();
    } catch (IOException e) {
      System.err.println("problem opening file" + filename);
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    TestTree tree = getExampleTree();
    System.out.println(tree);
    tree.renumber(0);
    System.out.println(tree);
    tree.outputGraphviz("/home/rmelick/trees/testGraph.dot");
  }

  /**
   * parse an input string
   * @param input
   * @return
   */
//  public TestTree parse(String input) {
//    return input
//  }

  /**
   * Return the default example Tree
   * @return
   */
  public static TestTree getExampleTree() {
    TestTree tree = new TestTree("TOK_QUERY");
    tree.addChild(new TestTree("TOK_FROM"));
    tree.addChild(new TestTree("TOK_INSERT"));
    tree.getChildren().get(0).addChild(new TestTree("TOK_TABREF"));
    TestTree right = tree.getChildren().get(1);
    right.addChild(new TestTree("TOK_DESTINATION"));
    right.addChild(new TestTree("TOK_SELECT"));

    return tree;
  }

}

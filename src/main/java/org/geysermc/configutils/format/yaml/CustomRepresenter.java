package org.geysermc.configutils.format.yaml;

import java.util.Arrays;
import java.util.stream.Collectors;
import org.geysermc.configutils.node.util.NodeWithComment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

final class CustomRepresenter extends Representer {
  public CustomRepresenter(DumperOptions options) {
    super(options);
    representers.put(NodeWithComment.class, new NodeWithCommentRepresent());
  }

  protected final class NodeWithCommentRepresent implements Represent {
    @Override
    public Node representData(Object data) {
      NodeWithComment withComment = (NodeWithComment) data;
      Node node = represent(withComment.value());
      node.setBlockComments(
          Arrays.stream(withComment.comment().split("\n"))
              .map(comment -> new CommentLine(null, null, comment, CommentType.BLOCK))
              .collect(Collectors.toList())
      );
      return node;
    }
  }
}

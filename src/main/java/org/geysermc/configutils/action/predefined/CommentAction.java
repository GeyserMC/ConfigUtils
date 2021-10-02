package org.geysermc.configutils.action.predefined;

import org.geysermc.configutils.action.ActionResult;
import org.geysermc.configutils.action.SingleAction;
import org.geysermc.configutils.action.storage.Storables;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;

public class CommentAction implements SingleAction {
  @Override
  public String friendlyName() {
    return "comment";
  }

  @Override
  public char actionPrefix() {
    return '#';
  }

  @Override
  public ActionResult handle(
      String strippedLine,
      Storables storables,
      Placeholders placeholders,
      TemplateReader templateReader) {
    // comments are just... comments. We don't have to see them
    return ActionResult.ok();
  }

  @Override
  public SingleAction newInstance() {
    return new CommentAction();
  }
}

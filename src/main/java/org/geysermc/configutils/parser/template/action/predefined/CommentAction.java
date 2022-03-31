package org.geysermc.configutils.parser.template.action.predefined;

import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.parser.template.action.ActionResult;
import org.geysermc.configutils.parser.template.action.SingleAction;
import org.geysermc.configutils.parser.template.action.storage.Storables;

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

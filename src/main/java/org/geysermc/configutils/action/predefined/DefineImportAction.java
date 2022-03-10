package org.geysermc.configutils.action.predefined;

import java.util.List;
import org.geysermc.configutils.action.ActionResult;
import org.geysermc.configutils.action.SingleAction;
import org.geysermc.configutils.action.predefined.ImportSectionAction.LastImportSection;
import org.geysermc.configutils.action.storage.Storable;
import org.geysermc.configutils.action.storage.Storables;
import org.geysermc.configutils.action.storage.Unfinished;
import org.geysermc.configutils.action.storage.predefined.UsedConfigsStorage;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.TemplateParseResult;
import org.geysermc.configutils.parser.TemplateParser;
import org.geysermc.configutils.parser.placeholder.Placeholders;

public class DefineImportAction implements SingleAction, Storable {
  private String templateName;
  private List<String> lines;

  @Override
  public String friendlyName() {
    return "define import";
  }

  @Override
  public char actionPrefix() {
    return ' ';
  }

  @Override
  @SuppressWarnings("ConstantConditions")
  public ActionResult handle(
      String strippedLine,
      Storables storables,
      Placeholders placeholders,
      TemplateReader templateReader) {

    Unfinished unfinished = storables.firstUnfinished(ImportSectionAction.class);
    if (unfinished != null) {
      return ActionResult.failed(unfinished.unfinishedMessage(this));
    }

    storables.removeFirst(LastImportSection.class);
    storables.removeFirst(DefineImportAction.class);

    templateName = strippedLine;
    try {
      lines = templateReader.readLines(templateName);
    } catch (Exception e) {
      return ActionResult.failed(String.format(
          "Unable to read import called '%s', does it exist?",
          templateName
      ));
    }

    TemplateParseResult result =
        storables.first(TemplateParser.class)
            .parseTemplate(templateName, placeholders);

    if (!result.succeeded()) {
      return ActionResult.failed(result.error());
    }

    lines = result.templateLines();

    storables.add(this);
    storables.first(UsedConfigsStorage.class).addUsed(templateName);
    return ActionResult.ok();
  }

  public String configName() {
    return templateName;
  }

  public List<String> lines() {
    return lines;
  }

  @Override
  public SingleAction newInstance() {
    return new DefineImportAction();
  }
}

package org.geysermc.configutils.parser.template.action.predefined;

import java.util.ArrayList;
import java.util.List;
import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.parser.template.action.ActionResult;
import org.geysermc.configutils.parser.template.action.SingleAction;
import org.geysermc.configutils.parser.template.action.predefined.ImportSectionAction.LastImportSection;
import org.geysermc.configutils.parser.template.action.storage.Storables;
import org.geysermc.configutils.parser.template.action.storage.Unfinished;

public class WriteRemainingAction implements SingleAction {
  @Override
  public String friendlyName() {
    return "write remaining";
  }

  @Override
  public char actionPrefix() {
    return '*';
  }

  @Override
  public ActionResult handle(
      String strippedLine,
      Storables storables,
      Placeholders placeholders,
      TemplateReader templateReader) {

    Unfinished unfinished = storables.firstUnfinished(ImportSectionAction.class);
    if (unfinished != null) {
      return ActionResult.failed(unfinished.unfinishedMessage(this));
    }

    DefineImportAction defineImport = storables.first(DefineImportAction.class);
    if (defineImport == null) {
      return ActionResult.failed(
          "Cannot write the remaining of a config without defining a config");
    }

    int startLine = 0;
    LastImportSection lastImport = storables.removeFirst(LastImportSection.class);
    if (lastImport != null) {
      startLine = lastImport.lastLine();
    }

    List<String> lines = defineImport.lines();

    List<String> linesToAdd = new ArrayList<>();
    for (int i = startLine; i < lines.size(); i++) {
      linesToAdd.add(placeholders.replacePlaceholders(lines.get(i)));
    }

    storables.add(new LastImportSection(lines.size() - 1));

    return ActionResult.addLines(linesToAdd);
  }

  @Override
  public SingleAction newInstance() {
    return new WriteRemainingAction();
  }
}

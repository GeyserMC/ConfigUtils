package org.geysermc.configutils.parser.template.action;

import org.geysermc.configutils.file.template.TemplateReader;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.parser.template.action.storage.Storables;

public interface SingleAction extends Action {
  String friendlyName();

  char actionPrefix();

  ActionResult handle(
      String strippedLine,
      Storables storables,
      Placeholders placeholders,
      TemplateReader templateReader
  );

  SingleAction newInstance();
}

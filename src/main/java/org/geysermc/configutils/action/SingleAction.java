package org.geysermc.configutils.action;

import org.geysermc.configutils.action.storage.Storables;
import org.geysermc.configutils.parser.placeholder.Placeholders;
import org.geysermc.configutils.file.template.TemplateReader;

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

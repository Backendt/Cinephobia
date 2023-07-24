package fr.backendt.cinephobia;

import fr.backendt.cinephobia.models.Media;
import fr.backendt.cinephobia.models.Platform;
import fr.backendt.cinephobia.models.Trigger;
import fr.backendt.cinephobia.models.Warn;

import java.util.List;

public class TestingModelValues {

    public static final Platform PLATFORM_TEST = new Platform("JUnit TV");
    public static final Media MEDIA_TEST = new Media("Java Testing: The Revenge", "https://example.com/hello.png",List.of(PLATFORM_TEST));
    public static final Trigger TRIGGER_TEST = new Trigger("Technophobia", "Fear of technology");
    public static final Warn WARN_TEST = new Warn(TRIGGER_TEST, MEDIA_TEST, 3);
}

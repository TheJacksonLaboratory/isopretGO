package org.jax.isopret.interpro;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.model.InterproAnnotation;
import org.jax.isopret.model.InterproEntry;

import java.util.Optional;

import static org.jax.isopret.model.InterproEntryType.ACTIVE_SITE;
import static org.jax.isopret.model.InterproEntryType.FAMILY;

public class InterproTestBase {

    public static final InterproEntry IPR000138 = IPR000138();

    public static final InterproEntry IPR000276 = IPR000276();



    public static final InterproAnnotation ipr000276annotation = ipr000276annotation();

    public static final InterproAnnotation ipr000276annotationShiftedRight = ipr000276annotationShiftedRight();

    public static final InterproAnnotation ipr000276annotationSHiftedLeft = ipr000276annotationShiftedLeft();

    public static final InterproAnnotation ipr000276annotationIncluded = ipr000276annotationIncluded();

    public static final InterproAnnotation ipr000276annotationComprises = ipr000276annotationComprises();

    private static InterproEntry IPR000138() {
        return new InterproEntry("IPR000138", ACTIVE_SITE, "Hydroxymethylglutaryl-CoA lyase, active site");
    }

    private static InterproEntry IPR000276() {
        return new InterproEntry("IPR000276", FAMILY, "G protein-coupled receptor, rhodopsin-like");

    }


    private static InterproAnnotation ipr000276annotation() {
        String line = "ENST00000641515	ENSG00000186092	IPR000276	40	64";
        Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
        if (opt.isEmpty()) {
            // should really never happen
            throw new IsopretRuntimeException("Could not construct interpro annotation in test class");
        }
        return opt.get();
    }


    private static InterproAnnotation ipr000276annotationShiftedRight() {
        String line = "ENST00000641515	ENSG00000186092	IPR000276	42	70";
        Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
        if (opt.isEmpty()) {
            // should really never happen
            throw new IsopretRuntimeException("Could not construct interpro annotation in test class");
        }
        return opt.get();
    }

    private static InterproAnnotation ipr000276annotationShiftedLeft() {
        String line = "ENST00000641515	ENSG00000186092	IPR000276	27	52";
        Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
        if (opt.isEmpty()) {
            // should really never happen
            throw new IsopretRuntimeException("Could not construct interpro annotation in test class");
        }
        return opt.get();
    }

    private static InterproAnnotation ipr000276annotationIncluded() {
        String line = "ENST00000641515	ENSG00000186092	IPR000276	45	60";
        Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
        if (opt.isEmpty()) {
            // should really never happen
            throw new IsopretRuntimeException("Could not construct interpro annotation in test class");
        }
        return opt.get();
    }

    private static InterproAnnotation ipr000276annotationComprises() {
        String line = "ENST00000641515	ENSG00000186092	IPR000276	30	80";
        Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
        if (opt.isEmpty()) {
            // should really never happen
            throw new IsopretRuntimeException("Could not construct interpro annotation in test class");
        }
        return opt.get();
    }



}

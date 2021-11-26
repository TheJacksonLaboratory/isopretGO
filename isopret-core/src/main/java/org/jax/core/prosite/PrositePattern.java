package org.jax.core.prosite;

import org.jax.core.except.IsopretRuntimeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrositePattern {

    private final String prositeId;
    private final String prositeAccession;
    private final String description;
    /** A regular expression in prosite syntax, e.g., [RH]-G-x(2)-P-x-G(3)-x-[LIV] */
    private final String prositePattern;
    private final Pattern pattern;

    private final static String VALID_AMINOACIDS = "ARNDCEQGHILKMFPSTWYV";

    public PrositePattern(String ID, String AC, String DE, String PA) {
        if (ID.endsWith(";")) {
            this.prositeId = ID.substring(0,ID.length()-1);
        } else {
            this.prositeId = ID;
        }
        if (AC.endsWith(";")) {
            this.prositeAccession = AC.substring(0,AC.length()-1);
        } else {
            this.prositeAccession = AC;
        }
        this.description = DE;
        if (PA.endsWith(".")) {
            this.prositePattern = PA.substring(0,PA.length()-1);
        } else {
            this.prositePattern = PA;
        }
        // transform the prositePattern to a Java regular expression
        StringBuilder sb = new StringBuilder();
        String [] elems = this.prositePattern.split("-");
        for (String elem : elems) {
            String count = "";
            if (elem.startsWith("[") && elem.endsWith("]")) {
                // alternatives., e.g., [RH] -- same syntax for regex
                sb.append(elem);
            } else if (elem.contains("(") && elem.contains(")") ) {
                // something like this x(2) or this x(10,11)
                int i = elem.indexOf("(");
                int j = elem.indexOf(")");
                count = elem.substring(i+1,j);
                String amino = elem.substring(0,i);
                if (amino.equals("x")) {
                    sb.append(".{").append(count).append("}");
                } else if (amino.startsWith("{") && amino.endsWith("}")) {
                    j = amino.indexOf("}");
                    String excluded = String.format("[^%s]",amino.substring(1,j));
                    sb.append(excluded).append("{").append(count).append("}");
                } else {
                    sb.append(amino).append("{").append(count).append("}");
                }
            } else if (elem.equalsIgnoreCase("x")) {
                // wildcard
                sb.append(".");
            } else if (elem.startsWith("{") && elem.endsWith("}")) {
                int j = elem.indexOf("}");
                String excluded = String.format("[^%s]",elem.substring(1,j));
                sb.append(excluded);
            } else if (elem.length() == 1 && VALID_AMINOACIDS.contains(elem)) {
                sb.append(elem);
            } else if (elem.endsWith(">")) {
                sb.append(elem.replace(">", "$"));
            } else if (elem.startsWith("<")) {
                sb.append(elem.replace("<", "^"));
            } else {
                throw new IsopretRuntimeException("Could not deal with " + elem + " in " + this.prositePattern);
            }
        }
        pattern = Pattern.compile(sb.toString());
    }



    @Override
    public int hashCode() {
        return Objects.hash(this.prositeId, this.prositeAccession, this.prositePattern);
    }

    @Override
    public boolean equals(Object obj) {
        if (! (obj instanceof PrositePattern)) {
            return false;
        }
        PrositePattern that = (PrositePattern) obj;
        return (this.prositeId.equals((that.prositeId)) &&
                this.prositeAccession.equals(that.prositeAccession) &&
                this.prositePattern.equals(that.prositePattern));

    }


    @Override
    public String toString() {
        return String.format("ID: %s; AC: %s; PA: %s; regex: %s",
                this.prositeId, this.prositeAccession, this.prositePattern, this.pattern);
    }

    public boolean matchesSequence(String seq) {
        seq = seq.toUpperCase();
        Matcher matcher = pattern.matcher(seq);
        return matcher.matches();
    }

    public List<Integer> getPositions(String sequence) {
        List<Integer> pos = new ArrayList<>();
        Matcher matcher = pattern.matcher(sequence);
        while(matcher.find()) {
            pos.add(matcher.start());
        }
        return pos;
    }

    public String getPrositeId() {
        return prositeId;
    }

    public String getPrositeAccession() {
        return prositeAccession;
    }

    public String getDescription() {
        return description;
    }

    public String getPrositePattern() {
        return prositePattern;
    }

    public Pattern getPattern() {
        return pattern;
    }
}

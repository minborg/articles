package cmdold;

import java.util.Optional;

public abstract class AbstractCla implements Cla {

    private final String shortName;
    private final String name;
    private final String description;
    private final boolean required;
    private final int maxCardinality;

    public AbstractCla(String shortName) {
        this(shortName, null, null, true, 1);
    }

    public AbstractCla(String shortName,
                       String name) {
        this(shortName, name, null, true, 1);
    }

    public AbstractCla(String shortName,
                       String name,
                       String description) {
        this(shortName, name, description, true, 1);
    }

    public AbstractCla(String shortName,
                       String name,
                       String description,
                       boolean required) {
        this(shortName, name, description, required, 1);
    }

    public AbstractCla(String shortName,
                       String name,
                       String description,
                       boolean required,
                       int maxCardinality) {
        this.shortName = shortName;
        this.name = name;
        this.description = description;
        this.required = required;
        this.maxCardinality = maxCardinality;
    }

    public String shortName() {
        return shortName;
    }

    public Optional<String> name() {
        return Optional.ofNullable(name);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public boolean required() {
        return required;
    }

    public int maxCardinality() {
        return maxCardinality;
    }
}

package licenta.validator;

public enum ValidationMode {
    /**
     * Entity is validated before it's created
     */
    CREATE,
    /**
     * Entity is validated before it's updated (replaced)
     */
    UPDATE
}

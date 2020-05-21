package de.tautenhahn.easydata;

import java.util.ArrayList;

/**
 * Contains information about a problem resolving special tags, especially where the respective tag is in source code.
 */
public class ResolverException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    private final ArrayList<Token> locationInTemplate = new ArrayList<>(); //NOPMD need serializable field type

    /**
     * Creates instance.
     *
     * @param message describes problem
     */
    public ResolverException(String message)
    {
        super(message);
    }

    /**
     * Creates instance
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).  (A {@code
     *     null} value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public ResolverException(String message, Exception cause)
    {
        super(message, cause);
    }

    @Override
    public String getMessage()
    {
        StringBuffer result = new StringBuffer(super.getMessage());
        if (!locationInTemplate.isEmpty())
        {
            result.append("\n   when resolving tag");
            locationInTemplate.forEach(t -> result.append("\n   ").append(t));
        }
        return result.toString();
    }

    /**
     * Adds additional context information
     *
     * @param src token which is currently resolved
     */
    void addLocation(Token src)
    {
        locationInTemplate.add(src);
    }
}

package com.library.messages;

/**
 * Short, plain-language text shown to library staff and patrons. Technical detail belongs in logs only.
 */
public final class UserFacingMessages {

    public static final String GENERIC_TRY_AGAIN =
            "Something went wrong. Please wait a moment and try again. If the problem continues, contact the library.";

    public static final String REGISTRATION_UNAVAILABLE =
            "We couldn't complete your registration. Please check your details and try again. If it keeps happening,"
                    + " contact the library for help.";

    public static final String REGISTRATION_SAVE_FAILED_GENERIC =
            "We could not save your registration. Check that the database is running and your details are correct, then"
                    + " try again. If this keeps happening, contact the library.";

    public static final String REGISTRATION_DUPLICATE_EMAIL =
            "That email is already registered. Try signing in, or use a different email address.";

    public static final String REGISTRATION_SAVE_FAILED_SCHEMA =
            "We could not save your registration. Please try again in a moment. If this keeps happening, contact the"
                    + " library for help.";

    /** @deprecated prefer specific registration messages above */
    @Deprecated
    public static final String REGISTRATION_DATA_SAVE_FAILED = REGISTRATION_SAVE_FAILED_GENERIC;

    public static final String DATA_CONFLICT =
            "This information conflicts with an existing record (for example, the email may already be in use)."
                    + " Please review what you entered and try again.";

    public static final String PROFILE_PHOTO_TOO_LARGE =
            "That profile photo is over the size limit, so nothing was saved. Please choose a smaller image and try"
                    + " again.";

    public static final String PAGE_NOT_FOUND = "We could not find that page. Check the address or go back to the home page.";

    /** Use when a domain exception message might be missing; avoids showing a blank banner. */
    public static String orGeneric(String message) {
        return message != null && !message.isBlank() ? message : GENERIC_TRY_AGAIN;
    }

    private UserFacingMessages() {}
}

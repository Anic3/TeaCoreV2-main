package me.tuanvo0022.discord.link;

import me.tuanvo0022.Main;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class LinkCodeManager {

    private final SecureRandom random = new SecureRandom();

    // code -> LinkCode
    private final Map<String, LinkCode> codes = new ConcurrentHashMap<>();

    // discordId -> code (QUAN TRỌNG)
    private final Map<Long, String> discordCodes = new ConcurrentHashMap<>();

    private final String charset;
    private final int length;
    private final long expireMillis;

    public LinkCodeManager(Main plugin) {
        this.charset = plugin.config().LINK_CODE_CHARSET;
        this.length = plugin.config().LINK_CODE_LENGTH;
        this.expireMillis = plugin.config().LINK_CODE_EXPIRE_SECONDS * 1000L;
    }

    /**
     * Generates a unique code for a given discordId.
     * 
     * @param discordId
     *   the discordId to generate the code for
     * @return the generated code
     */
    public synchronized String create(long discordId) {
        cleanupExpired();

        String existingCode = discordCodes.get(discordId);
        if (existingCode != null) {
            LinkCode cached = codes.get(existingCode);
            if (cached != null && !cached.isExpired()) {
                return existingCode;
            }

            invalidate(existingCode);
        }

        String code;
        do {
            code = randomCode();
        } while (codes.containsKey(code));

        LinkCode linkCode = new LinkCode(
                code,
                discordId,
                System.currentTimeMillis() + expireMillis
        );

        codes.put(code, linkCode);
        discordCodes.put(discordId, code);

        return code;
    }

    /**
     * Returns a given code.
     * @param code
     *   the code to be returned
     * @return
     *   the code if it exists, null otherwise
     */
    public LinkCode get(String code) {
        LinkCode c = codes.get(code);
        if (c == null) return null;

        if (c.isExpired()) {
            invalidate(code);
            return null;
        }
        return c;
    }

    /**
     * Consumes a given code.
     * Removes the code from memory and invalidates the code in the database if it is not expired.
     * @param code
     *   the code to be consumed
     * @return
     *   true if the code was removed from memory, false otherwise
     */
    public synchronized LinkCode consume(String code) {
        LinkCode c = get(code);
        if (c == null) return null;

        invalidate(code);
        return c;
    }
    
    /**
     * Invalidates a given code.
     * Removes the code from memory and invalidates the code in the database if it is not expired.
     * @param code
     *   the code to be invalidated
     * @return
     *   true if the code was removed from memory, false otherwise
     */
    private void invalidate(String code) {
        LinkCode removed = codes.remove(code);
        if (removed != null) {
            discordCodes.remove(removed.getDiscordId());
        }
    }

    /**
     * Generates a random string of a given length.
     * @param length
     *   the length of the string to be generated
     * @return
     *   a string of length <code>length> containing random characters
     */
    private String randomCode() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(random.nextInt(charset.length())));
        }
        return sb.toString();
    }

    /**
     * Removes all expired codes from memory.
     * @param now The current time in milliseconds.
     * @return true if there are expired codes to remove, false otherwise.
     */
    public void cleanupExpired() {
        long now = System.currentTimeMillis();
        codes.entrySet().removeIf(entry -> {
            if (entry.getValue().getExpiresAt() <= now) {
                discordCodes.remove(entry.getValue().getDiscordId());
                return true;
            }
            return false;
        });
    }

    public void close() {
        codes.clear();
        discordCodes.clear();
    }
}

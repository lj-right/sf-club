package com.suifeng.server.memory;

import com.suifeng.server.memory.Entity.ChatMemoryRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class ChatMemoryRepository {

    private static final short UN_DELETED = 0;
    private static final short DELETED = 1;

    private static final RowMapper<ChatMemoryRecord> ROW_MAPPER = (rs, rowNum) -> ChatMemoryRecord.builder()
            .memoryId(rs.getLong("memory_id"))
            .messages(rs.getString("messages"))
            .createAt(toLocalDateTime(rs.getTimestamp("create_at")))
            .updateAt(toLocalDateTime(rs.getTimestamp("update_at")))
            .isDeleted(rs.getShort("is_deleted"))
            .build();

    private final JdbcTemplate jdbcTemplate;

    public ChatMemoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<ChatMemoryRecord> findById(Long memoryId) {
        List<ChatMemoryRecord> records = jdbcTemplate.query(
                "SELECT memory_id, messages::text AS messages, create_at, update_at, is_deleted "
                        + "FROM chat_memory WHERE memory_id = ? AND is_deleted = ?",
                ROW_MAPPER,
                memoryId,
                UN_DELETED
        );
        return records.stream().findFirst();
    }

    public List<ChatMemoryRecord> findAllOrderByUpdateAtDesc() {
        return jdbcTemplate.query(
                "SELECT memory_id, messages::text AS messages, create_at, update_at, is_deleted "
                        + "FROM chat_memory WHERE is_deleted = ? ORDER BY update_at DESC",
                ROW_MAPPER,
                UN_DELETED
        );
    }

    public void upsert(Long memoryId, String messagesJson) {
        jdbcTemplate.update(
                "INSERT INTO chat_memory (memory_id, messages, is_deleted, create_at, update_at) "
                        + "VALUES (?, ?::json, ?, now(), now()) "
                        + "ON CONFLICT (memory_id) DO UPDATE "
                        + "SET messages = EXCLUDED.messages, is_deleted = ?, update_at = now()",
                memoryId,
                messagesJson,
                UN_DELETED,
                UN_DELETED
        );
    }

    public boolean softDeleteById(Long memoryId) {
        int affectedRows = jdbcTemplate.update(
                "UPDATE chat_memory SET is_deleted = ?, update_at = now() "
                        + "WHERE memory_id = ? AND is_deleted = ?",
                DELETED,
                memoryId,
                UN_DELETED
        );
        return affectedRows > 0;
    }

    private static java.time.LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}

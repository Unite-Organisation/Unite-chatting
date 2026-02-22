CREATE INDEX idx_message_conversation_pagination ON message (conversation_id, send_at);
CREATE INDEX idx_conv_member_user ON conversation_member (user_id);
CREATE INDEX idx_conv_member_conv ON conversation_member (conversation_id);
CREATE INDEX idx_activity_user_status ON activity (user_id, status);
CREATE INDEX idx_conversation_updated_at_desc ON conversation (updated_at DESC);

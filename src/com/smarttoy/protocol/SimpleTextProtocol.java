package com.smarttoy.protocol;

public class SimpleTextProtocol extends BaseProtocol {
	
	private String m_text = "";
	
	public SimpleTextProtocol() {
		super(BaseProtocol.PT_SIMPLE_TEXT);
	}
	
	public SimpleTextProtocol(byte[] data) {
		super(data);
	}

	public String getText() {
		return m_text;
	}
	
	public void setText(String text) {
		m_text = text;
	}

	@Override
	protected byte[] getContentData() {
		return m_text.getBytes();
	}

	@Override
	protected void setContentData(byte[] data) {
		m_text = new String(data);
	}

}

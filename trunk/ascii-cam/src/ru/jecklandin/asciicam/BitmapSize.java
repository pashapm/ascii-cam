package ru.jecklandin.asciicam;

class BitmapSize {
	
	public final int m_h;
	public final int m_w;

	BitmapSize(int w, int h) {
		this.m_w = w;
		this.m_h = h;
	}
	
	@Override
	public String toString() {
		return m_w+"x"+m_h;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		BitmapSize bm = (BitmapSize)o;
		return m_w == bm.m_w && m_h == bm.m_h;
	}
}
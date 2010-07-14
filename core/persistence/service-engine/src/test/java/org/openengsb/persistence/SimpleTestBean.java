package org.openengsb.persistence;

public class SimpleTestBean {
    public Integer id;
    public String content;
    public String moreContent;

    public SimpleTestBean() {
    }

    public SimpleTestBean(Integer id, String content) {
        this.id = id;
        this.content = content;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.content == null) ? 0 : this.content.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.moreContent == null) ? 0 : this.moreContent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SimpleTestBean other = (SimpleTestBean) obj;
        if (this.content == null) {
            if (other.content != null)
                return false;
        } else if (!this.content.equals(other.content))
            return false;
        if (this.id == null) {
            if (other.id != null)
                return false;
        } else if (!this.id.equals(other.id))
            return false;
        if (this.moreContent == null) {
            if (other.moreContent != null)
                return false;
        } else if (!this.moreContent.equals(other.moreContent))
            return false;
        return true;
    }

}

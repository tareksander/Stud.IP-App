package org.studip.unofficial_app.api.rest;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Objects;
public class StudipCollection<T> implements Serializable
{
    public LinkedHashMap<String,T> collection;
    public Pagination pagination;
    static class Pagination implements Serializable {
        public int total;
        public int offset;
        public int limit;
        public Links links;
        static class Links implements Serializable {
            public String first;
            public String previous;
            public String next;
            public String last;
            @Override
            public boolean equals(Object o)
            {
                if (this == o)
                {
                    return true;
                }
                if (o == null || getClass() != o.getClass())
                {
                    return false;
                }
                Links links = (Links) o;
                return Objects.equals(first, links.first) &&
                        Objects.equals(previous, links.previous) &&
                        Objects.equals(next, links.next) &&
                        Objects.equals(last, links.last);
            }
            @Override
            public int hashCode()
            {
                return Objects.hash(first, previous, next, last);
            }
        }
        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            Pagination that = (Pagination) o;
            return total == that.total &&
                    offset == that.offset &&
                    limit == that.limit &&
                    Objects.equals(links, that.links);
        }
        @Override
        public int hashCode()
        {
            return Objects.hash(total, offset, limit, links);
        }
    }
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        StudipCollection<?> that = (StudipCollection<?>) o;
        return Objects.equals(collection, that.collection) &&
                Objects.equals(pagination, that.pagination);
    }
    @Override
    public int hashCode()
    {
        return Objects.hash(collection, pagination);
    }
}

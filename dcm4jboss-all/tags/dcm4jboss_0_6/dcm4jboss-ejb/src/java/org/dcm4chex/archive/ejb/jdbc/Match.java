/* $Id: Match.java 945 2004-02-01 14:26:22Z gunterze $
 * Copyright (c) 2002,2003 by TIANI MEDGRAPH AG
 *
 * This file is part of dcm4che.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.dcm4chex.archive.ejb.jdbc;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @version $Revision: 945 $ $Date: 2004-02-01 22:26:22 +0800 (周日, 01 2月 2004) $
 * @since 25.08.2003
 */
abstract class Match
{

    private static final String DATE_FORMAT = "''yyyy-MM-dd HH:mm:ss.SSS''";
    protected final String column;
    protected final boolean type2;

    protected Match(String field, boolean type2)
    {
        this.column = JdbcProperties.getInstance().getProperty(field);
        if (column == null)
            throw new IllegalArgumentException("field: " + field);

        this.type2 = type2;
    }

    public boolean appendTo(StringBuffer sb)
    {
        if (isUniveralMatch())
            return false;
        sb.append('(');
        if (type2)
        {
            sb.append(column);
            sb.append(" IS NULL OR ");
        }
        appendBodyTo(sb);
        sb.append(')');
        return true;
    }

    public abstract boolean isUniveralMatch();
    protected abstract void appendBodyTo(StringBuffer sb);

    static class SingleValue extends Match
    {
        private final String value;
        public SingleValue(String field, boolean type2, String value)
        {
            super(field, type2);
            this.value = value;
        }

        public boolean isUniveralMatch()
        {
            return value == null || value.length() == 0;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            sb.append(column);
            sb.append(" = \'");
            sb.append(value);
            sb.append('\'');
        }
    }

    static class ListOfUID extends Match
    {
        private final String[] uids;
        public ListOfUID(String field, boolean type2, String[] uids)
        {
            super(field, type2);
            this.uids = uids != null ? (String[]) uids.clone() : new String[0];
        }

        public boolean isUniveralMatch()
        {
            return uids.length == 0;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            for (int i = 0; i < uids.length; i++)
            {
                if (i > 0)
                    sb.append(" OR ");
                sb.append(column);
                sb.append(" = \'");
                sb.append(uids[i]);
                sb.append('\'');
            }
        }
    }

    static class WildCard extends Match
    {
        private final char[] wc;
        private final boolean ignoreCase;
        public WildCard(
            String field,
            boolean type2,
            String wc,
            boolean ignoreCase)
        {
            super(field, type2);
            this.wc = wc != null ? wc.toCharArray() : new char[0];
            this.ignoreCase = ignoreCase;
        }

        public boolean isUniveralMatch()
        {
            for (int i = wc.length; --i >= 0;)
                if (wc[i] != '*')
                    return false;
            return true;
        }

        public boolean isLike()
        {
            for (int i = wc.length; --i >= 0;)
                if (wc[i] == '*' || wc[i] == '?')
                    return true;
            return false;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            if (ignoreCase)
                sb.append(" UPPER(");
            sb.append(column);
            if (ignoreCase)
                sb.append(')');
            final boolean like = isLike();
            sb.append(like ? " LIKE " : " = ");
            if (ignoreCase)
                sb.append(" UPPER(");

            sb.append('\'');
            char c;
            for (int i = 0; i < wc.length; i++)
            {
                switch (c = wc[i])
                {
                    case '?' :
                        c = '_';
                        break;
                    case '*' :
                        c = '%';
                        break;
                    case '\'' :
                        sb.append('\'');
                        break;
                    case '_' :
                    case '%' :
                        if (like) {
                            sb.append('\\');
                        }
                        break;
                }
                sb.append(c);
            }
            sb.append('\'');

            if (ignoreCase)
                sb.append(')');
        }

    }

    static class Range extends Match
    {
        private final Date[] range;
        public Range(String field, boolean type2, Date[] range)
        {
            super(field, type2);
            this.range = range != null ? (Date[]) range.clone() : null;
        }

        public boolean isUniveralMatch()
        {
            return range == null;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
            sb.append(column);
            if (range[0] == null)
            {
                sb.append(" <= ");
                sb.append(df.format(range[1]));
            } else if (range[1] == null)
            {
                sb.append(" >= ");
                sb.append(df.format(range[0]));
            } else
            {
                sb.append(" BETWEEN ");
                sb.append(df.format(range[0]));
                sb.append(" AND ");
                sb.append(df.format(range[1]));
            }

        }

    }

    static class ModalitiesInStudy extends Match
    {
        private final String md;
        public ModalitiesInStudy(String md)
        {
            super("Series.modality", false);
            this.md = md;
        }

        public boolean isUniveralMatch()
        {
            return md == null || md.length() == 0;
        }

        protected void appendBodyTo(StringBuffer sb)
        {
            JdbcProperties jp = JdbcProperties.getInstance();
            sb.append("(SELECT count(*) FROM ");
            sb.append(jp.getProperty("Series"));
            sb.append(" WHERE ");
            sb.append(jp.getProperty("Series.study_fk"));
            sb.append(" = ");
            sb.append(jp.getProperty("Study.pk"));
            sb.append(" AND ");
            sb.append(column);
            sb.append(" = '");
            sb.append(md);
            sb.append("') > 0");
        }
    }
}

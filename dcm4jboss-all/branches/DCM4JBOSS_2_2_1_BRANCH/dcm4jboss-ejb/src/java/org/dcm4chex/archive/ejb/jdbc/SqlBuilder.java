/******************************************
 *                                        *
 *  dcm4che: A OpenSource DICOM Toolkit   *
 *                                        *
 *  Distributable under LGPL license.     *
 *  See terms of license at gnu.org.      *
 *                                        *
 ******************************************/
package org.dcm4chex.archive.ejb.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Gunter.Zeilinger@tiani.com
 * @author Harald.Metterlein@heicare.com
 * @version $Revision: 1582 $ $Date: 2005-03-03 00:53:04 +0800 (周四, 03 3月 2005) $
 * @since 26.08.2003
 */
class SqlBuilder {

    public static final boolean TYPE1 = false;
    public static final boolean TYPE2 = true;
    public static final String DESC = " DESC";
    public static final String ASC = " ASC";
    public static final String WHERE = " WHERE ";
    public static final String AND = " AND ";
    public static final String[] SELECT_COUNT = { "count(*)" };
    private String[] select;
    private String[] from;
    private String[] leftJoin;
    private String[] relations;
    private ArrayList matches = new ArrayList();
    private ArrayList orderby = new ArrayList();
    private int limit = 0;
    private int offset = 0;
    private String whereOrAnd = WHERE;

    private static int getDatabase() {
        return JdbcProperties.getInstance().getDatabase();
    }

    public void setSelect(String[] fields) {
        select = JdbcProperties.getInstance().getProperties(fields);
    }

    public void setSelectCount() {
        select = SELECT_COUNT;
    }

    public void setFrom(String[] entities) {
        JdbcProperties jp = JdbcProperties.getInstance();
        from = jp.getProperties(entities);
    }

    public void setLeftJoin(String[] leftJoin) {
        if (leftJoin == null) {
            this.leftJoin = null;
            return;
        }
        if (leftJoin.length % 3 != 0) {
            throw new IllegalArgumentException("" + Arrays.asList(leftJoin));
        }
        this.leftJoin = JdbcProperties.getInstance().getProperties(leftJoin);
    }

    public void addOrderBy(String field, String order) {
        orderby.add(JdbcProperties.getInstance().getProperty(field) + order);
    }

    public final void setLimit(int limit) {
        this.limit = Math.max(0, limit);
    }

    public final void setOffset(int offset) {
        this.offset = Math.max(0, offset);
    }

    public void setRelations(String[] relations) {
        if (relations == null) {
            this.relations = null;
            return;
        }
        if ((relations.length & 1) != 0) {
            throw new IllegalArgumentException(
                "relations[" + relations.length + "]");
        }
        this.relations = JdbcProperties.getInstance().getProperties(relations);
    }

    private void addMatch(Match match) {
        if (!match.isUniveralMatch())
            matches.add(match);
    }
    
    public void addNULLValueMatch( String field, boolean inverter ) {
    	addMatch( new Match.NULLValue( field, inverter ) );
    }

    public void addSingleValueMatch(
        String field,
        boolean type2,
        String value) {
        addMatch(new Match.SingleValue(field, type2, value));
    }

    public void addLiteralMatch(
            String field,
            boolean type2,
            String literal) {
        addMatch(new Match.AppendLiteral(field, type2, literal));
    }
    
    public void addBooleanMatch(
            String field,
            boolean type2,
            boolean value) {
        addMatch(new Match.AppendLiteral(field, type2, toBooleanLiteral(value)));
    }
    
    private String toBooleanLiteral(boolean value) {
        switch (getDatabase()) {
        case JdbcProperties.DB2 :
        case JdbcProperties.ORACLE :
        case JdbcProperties.MYSQL :
            return value ? " != 0" : " = 0";
        default:
            return value ? " = true" : " = false";
        }
    }

    public void addListOfUidMatch(String field, boolean type2, String[] uids) {
        addMatch(new Match.ListOfUID(field, type2, uids));
    }

    public void addWildCardMatch(
        String field,
        boolean type2,
        String wc,
        boolean ignoreCase) {
        addMatch(new Match.WildCard(field, type2, wc, ignoreCase));
    }

    public void addRangeMatch(String field, boolean type2, Date[] range) {
        addMatch(new Match.Range(field, type2, range));
    }

    public void addModalitiesInStudyMatch(String md) {
        addMatch(new Match.ModalitiesInStudy(md));
    }

    public String getSql() {
        if (select == null)
            throw new IllegalStateException("select not initalized");
        if (from == null)
            throw new IllegalStateException("from not initalized");

        StringBuffer sb = new StringBuffer("SELECT ");
        if (limit > 0 || offset > 0) {
            switch (getDatabase()) {
                case JdbcProperties.HSQL :
                    sb.append("LIMIT ");
                    sb.append(offset);
                    sb.append(" ");
                    sb.append(limit);
                    sb.append(" ");
                    appendTo(sb, select);
                    break;
                case JdbcProperties.DB2 :
                    sb.append("* FROM ( SELECT ");
                    appendTo(sb, select);
                    sb.append(", ROW_NUMBER() OVER (ORDER BY ");
                    appendTo(
                        sb,
                        (String[]) orderby.toArray(new String[orderby.size()]));
                    sb.append(") AS rownum ");
                    break;
                case JdbcProperties.ORACLE :
                    sb.append("* FROM ( SELECT ");
                    appendTo(sb, selectC1C2CN());
                    sb.append(", ROWNUM as r1 FROM ( SELECT ");
                    appendTo(sb, selectAsC1C2CN());
                    break;
                default:
                    appendTo(sb, select);
                    break;
            }
        } else {
            appendTo(sb, select);            
        }
        sb.append(" FROM ");
      	appendInnerJoinsToFrom(sb);
        appendLeftJoinToFrom(sb);
        whereOrAnd = WHERE;
       	appendInnerJoinsToWhere(sb);
        appendLeftJoinToWhere(sb);
        appendMatchesTo(sb);
        if (!orderby.isEmpty()) {
            sb.append(" ORDER BY ");
            appendTo(
                sb,
                (String[]) orderby.toArray(new String[orderby.size()]));
        }
        if (limit > 0 || offset > 0) {
            switch (getDatabase()) {
                case JdbcProperties.PSQL :
                case JdbcProperties.MYSQL :
                    sb.append(" LIMIT ");
                    sb.append(limit);
                    sb.append(" OFFSET ");
                    sb.append(offset);
                    break;
                case JdbcProperties.DB2 :
                    sb.append(" ) AS foo WHERE rownum > ");
                    sb.append(offset);
                    sb.append(" AND rownum <= ");
                    sb.append(offset + limit);
                    break;
                case JdbcProperties.ORACLE :
                    sb.append(" ) WHERE ROWNUM <= ");
                    sb.append(offset + limit);
                    sb.append(" ) WHERE r1 > ");
                    sb.append(offset);
                    break;
            }
        }
        if (getDatabase() == JdbcProperties.DB2)
            sb.append(" FOR READ ONLY");
        return sb.toString();
    }

    private String[] selectC1C2CN() {
        String[] retval = new String[select.length]; 
        for (int i = 0; i < retval.length; i++)
            retval[i] = "c" + (i+1);
        return retval;
    }

    private String[] selectAsC1C2CN() {
        String[] retval = new String[select.length]; 
        for (int i = 0; i < retval.length; i++)
            retval[i] = select[i] + " AS c" + (i+1);
        return retval;
    }

    private void appendTo(StringBuffer sb, String[] a) {
        for (int i = 0; i < a.length; i++) {
            if (i > 0)
                sb.append(", ");
            sb.append(a[i]);
        }
    }

    private void appendLeftJoinToFrom(StringBuffer sb) {
        if (leftJoin == null) return;
        for (int i = 0, n = leftJoin.length/3; i < n; ++i) {
            final int i3 = 3*i;
	        if (getDatabase() == JdbcProperties.ORACLE) {
	            sb.append(", ");
	            sb.append(leftJoin[i3]);            
	        } else {
		        sb.append(" LEFT JOIN ");
		        sb.append(leftJoin[i3]);
		        sb.append(" ON (");
		        sb.append(leftJoin[i3+1]);
		        sb.append(" = ");
		        sb.append(leftJoin[i3+2]);
		        sb.append(")");
	        }
        }
    }

    private void appendLeftJoinToWhere(StringBuffer sb) {
        if (leftJoin == null || getDatabase() != JdbcProperties.ORACLE) return;
        for (int i = 0, n = leftJoin.length/3; i < n; ++i) {
            final int i3 = 3*i;
	        sb.append(whereOrAnd);
	        whereOrAnd = AND;
	        sb.append(leftJoin[i3+1]);
	        sb.append(" = ");
	        sb.append(leftJoin[i3+2]);
	        sb.append("(+)");
        }
    }
        
	private void appendInnerJoinsToFrom(StringBuffer sb) {
		if (relations == null || getDatabase() == JdbcProperties.ORACLE) {
			appendTo(sb,from);
		} else {
			sb.append(from[0]);
			for (int i = 0, n = relations.length/2; i < n; ++i) {
			    final int i2 = 2*i;
				sb.append(" INNER JOIN ");
				sb.append(from[i+1]);
				sb.append(" ON (");
				sb.append(relations[i2]);
				sb.append(" = ");
				sb.append(relations[i2+1]);
				sb.append(")");
			}
		}
	}
	
	private void appendInnerJoinsToWhere(StringBuffer sb) {
		if (relations == null || getDatabase() != JdbcProperties.ORACLE) return;
        for (int i = 0, n = relations.length/2; i < n; ++i) {
            final int i2 = 2*i;
            sb.append(whereOrAnd);
            whereOrAnd = AND;
            sb.append(relations[i2]);
            sb.append(" = ");
            sb.append(relations[i2+1]);
        }
	}

    private void appendMatchesTo(StringBuffer sb) {
        if (matches == null) return;
        for (int i = 0; i < matches.size(); i++) {
            sb.append(whereOrAnd);
            whereOrAnd = AND;
            ((Match) matches.get(i)).appendTo(sb);
        }
    }
}

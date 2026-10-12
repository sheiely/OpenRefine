
package com.google.refine.commands.expr;

import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.google.refine.expr.EvalError;
import com.google.refine.expr.ExpressionUtils;
import com.google.refine.expr.HasFields;
import com.google.refine.expr.WrappedCell;
import com.google.refine.expr.WrappedRow;
import com.google.refine.util.ParsingUtilities;

public final class PreviewValueFormatter {

    private final Handler first;

    public PreviewValueFormatter() {
        Handler chain = new DefaultHandler(null);
        chain = new NumberHandler(chain);
        chain = new StringHandler(chain);
        chain = new DateHandler(chain);
        chain = new HasFieldsHandler(chain);
        chain = new ListHandler(chain);
        chain = new ArrayHandler(chain);
        chain = new JsonHandler(chain);
        chain = new WrappedRowHandler(chain);
        chain = new WrappedCellHandler(chain);
        chain = new NullHandler(chain);
        chain = new ErrorHandler(chain);

        first = chain;
    }

    public void writeValue(
            StringBuffer sb,
            Object value,
            boolean quote
    ) {
        first.handle(sb, value, quote, this);
    }

    private abstract static class Handler {

        private final Handler next;

        protected Handler(Handler next) {
            this.next = next;
        }

        public final void handle(
                StringBuffer sb,
                Object value,
                boolean quote,
                PreviewValueFormatter formatter
        ) {
            if (supports(value)) {
                write(sb, value, quote, formatter);
            } else if (next != null) {
                next.handle(sb, value, quote, formatter);
            }
        }

        protected abstract boolean supports(Object value);

        protected abstract void write(
                StringBuffer sb,
                Object value,
                boolean quote,
                PreviewValueFormatter formatter
        );
    }

    private static class ErrorHandler extends Handler {

        ErrorHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return ExpressionUtils.isError(value);
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append("[error: ")
              .append(((EvalError) value).message)
              .append("]");
        }
    }

    private static class NullHandler extends Handler {

        NullHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value == null;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append("null");
        }
    }

    private static class WrappedCellHandler extends Handler {

        WrappedCellHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof WrappedCell;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append("[object Cell]");
        }
    }

    private static class WrappedRowHandler extends Handler {

        WrappedRowHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof WrappedRow;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append("[object Row]");
        }
    }

    private static class JsonHandler extends Handler {

        JsonHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof ObjectNode
                    || value instanceof ArrayNode;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append(value.toString());
        }
    }

    private static class ArrayHandler extends Handler {

        ArrayHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return ExpressionUtils.isArray(value);
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            Object[] array = (Object[]) value;

            sb.append("[ ");

            for (int i = 0; i < array.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                formatter.writeValue(sb, array[i], true);
            }

            sb.append(" ]");
        }
    }

    private static class ListHandler extends Handler {

        ListHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return ExpressionUtils.isArrayOrList(value);
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            List<Object> list = ExpressionUtils.toObjectList(value);

            sb.append("[ ");

            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }

                formatter.writeValue(sb, list.get(i), true);
            }

            sb.append(" ]");
        }
    }

    private static class HasFieldsHandler extends Handler {

        HasFieldsHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof HasFields;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append("[object ")
              .append(value.getClass().getSimpleName())
              .append("]");
        }
    }

    private static class DateHandler extends Handler {

        DateHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof OffsetDateTime;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append("[date ")
              .append(ParsingUtilities.dateToString(
                      (OffsetDateTime) value))
              .append("]");
        }
    }

    private static class StringHandler extends Handler {

        StringHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof String;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            if (quote) {
                try {
                    sb.append(
                        ParsingUtilities.mapper
                            .writeValueAsString((String) value)
                    );
                } catch (JsonProcessingException e) {
                    // Preserva o tratamento original.
                }
            } else {
                sb.append((String) value);
            }
        }
    }

    private static class NumberHandler extends Handler {

        NumberHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return value instanceof Double
                    || value instanceof Float;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            Number n = (Number) value;

            if (n.doubleValue() - n.longValue() == 0.0) {
                sb.append(n.longValue());
            } else {
                sb.append(n.doubleValue());
            }
        }
    }

    private static class DefaultHandler extends Handler {

        DefaultHandler(Handler next) {
            super(next);
        }

        @Override
        protected boolean supports(Object value) {
            return true;
        }

        @Override
        protected void write(
                StringBuffer sb, Object value, boolean quote,
                PreviewValueFormatter formatter
        ) {
            sb.append(value.toString());
        }
    }
}

package ru.vrn.rt.analyzesystem.anlyze.excel.beans;

import ru.vrn.rt.analyzesystem.anlyze.excel.ExcelColumn;

import java.util.Collection;
import java.util.Objects;

    public class SumAndAvrAggregateBean {

        @ExcelColumn(name = "msisdn", order = 1)
        private String msisdn;
        @ExcelColumn(name = "кол-во", order = 2)
        private int count;
        @ExcelColumn(name = "длительность", order = 3)
        private int sum;
        @ExcelColumn(name = "среднее", order = 4)
        private double average;



        public SumAndAvrAggregateBean(String msisdn) {
            this.msisdn = msisdn;
            this.sum = 0;
            this.average = 0.0;
            this.count = 0;
        }

        public SumAndAvrAggregateBean() {
            this.sum = 0;
            this.average = 0.0;
            this.count = 0;
        }

        public void addValue(int value) {
            this.sum += value;
            this.count++;
            this.average = (double) sum / count;
        }

        public String getMsisdn() {
            return msisdn;
        }

        public int getSum() {
            return sum;
        }

        public double getAverage() {
            return average;
        }

        // Вспомогательные методы для массового добавления значений
        public void addValues(int... values) {
            for (int value : values) {
                addValue(value);
            }
        }

        public void addValues(Collection<Integer> values) {
            for (int value : values) {
                addValue(value);
            }
        }

        // Метод для сброса статистики
        public void reset() {
            this.sum = 0;
            this.average = 0.0;
            this.count = 0;
        }

        // Метод для получения количества (если все же нужен, но не как основное поле)
        protected int getCountInternal() {
            return count;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        @Override
        public String toString() {
            return String.format("AggregateBean{sum=%d, average=%.2f}", sum, average);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SumAndAvrAggregateBean that = (SumAndAvrAggregateBean) o;
            return sum == that.sum && Double.compare(that.average, average) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sum, average);
        }
    }
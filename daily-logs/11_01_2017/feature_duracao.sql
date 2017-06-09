select tid, min(time) as beginDate, max(time) as endDate, max(time) - min(time) as duration from taxicab2 group by tid;

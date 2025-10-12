package ru.otus.hw.shell;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ru.otus.hw.config.MigrationProperties;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;

@ShellComponent
@RequiredArgsConstructor
public class MigrationShellCommands {

    private static final DateTimeFormatter TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final JobOperator jobOperator;

    private final JobExplorer jobExplorer;

    private final MigrationProperties props;

    @ShellMethod(
            key = "run pg2mongo",
            value = "Запустить миграцию из PostgreSQL в MongoDB. Пример: run pg2mongo --truncate true"
    )
    public String runPgToMongo(
            @ShellOption(
                    help = "Чистить Mongo перед загрузкой",
                    defaultValue = "false") boolean truncate,
            @ShellOption(
                    help = "Инкремент с даты (ISO-8601) для комментариев, опционально",
                    defaultValue = ShellOption.NULL) String since
    ) throws Exception {
        Properties params = buildParams(truncate, since);
        long execId = jobOperator.start("pgToMongoJob", params);
        return "Запущен pgToMongoJob, executionId=" + execId + "; params=" + propsToString(params);
    }

    @ShellMethod(
            key = "run mongo2pg",
            value = "Запустить миграцию из MongoDB в PostgreSQL. Пример: run mongo2pg --truncate true"
    )
    public String runMongoToPg(
            @ShellOption(
                    help = "Чистить PG перед загрузкой",
                    defaultValue = "false") boolean truncate,
            @ShellOption(
                    help = "Инкремент с даты (ISO-8601) для комментариев, опционально",
                    defaultValue = ShellOption.NULL) String since
    ) throws Exception {
        Properties params = buildParams(truncate, since);
        long execId = jobOperator.start("mongoToPgJob", params);
        return "Запущен mongoToPgJob, executionId=" + execId + "; params=" + propsToString(params);
    }


    private Properties buildParams(boolean truncate, String since) {
        Properties p = new Properties();
        p.setProperty("truncateBeforeLoad", Boolean.toString(truncate));
        if (since != null && !since.isBlank()) {
            p.setProperty("since", since.trim());
        }
        p.setProperty("run.id", Long.toString(System.currentTimeMillis()));
        return p;
    }

    private static String propsToString(Properties p) {
        if (p == null || p.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String name : p.stringPropertyNames()) {
            if (!sb.isEmpty()) {
                sb.append(',');
            }
            sb.append(name).append('=').append(p.getProperty(name));
        }
        return sb.toString();
    }


    @ShellMethod(key = "stop", value = "Остановить выполнение по executionId")
    public String stop(@ShellOption(help = "Job Execution Id") long executionId) throws Exception {
        boolean requested = jobOperator.stop(executionId);
        return requested
                ? "Стоп запрошен для executionId=" + executionId
                : "Не удалось запросить стоп (возможно, уже завершён) executionId=" + executionId;
    }

    @ShellMethod(key = "restart", value = "Рестарт выполнить по executionId (если рестартуемый)")
    public String restart(
            @ShellOption(help = "Job Execution Id") long executionId,
            @ShellOption(
                    help = "Параметры, если нужно переопределить",
                    defaultValue = ShellOption.NULL) String params) throws Exception {
        String actualParams = (params == null || params.isBlank())
                ? jobOperator.getParameters(executionId)
                : params;
        long newExecId = jobOperator.restart(executionId);
        return "Рестарт выполнен: oldExecutionId=" + executionId + " -> newExecutionId=" + newExecId +
                "; params=" + actualParams;
    }


    @ShellMethod(key = "jobs", value = "Показать зарегистрированные джобы")
    public String jobs() throws Exception {
        List<String> names = new ArrayList<>(jobOperator.getJobNames());
        names.sort(Comparator.naturalOrder());
        return "Jobs: " + String.join(", ", names);
    }

    @ShellMethod(key = "executions", value = "Показать последние исполнения джоба по имени")
    public String executions(@ShellOption(help = "Имя джоба") String jobName,
                             @ShellOption(help = "Максимум строк", defaultValue = "10") int limit) {
        List<JobInstance> instances = jobExplorer.getJobInstances(jobName, 0, Math.max(1, limit));
        StringBuilder sb = new StringBuilder("Последние инстансы для ").append(jobName).append(":\n");
        for (JobInstance ji : instances) {
            List<JobExecution> execs = jobExplorer.getJobExecutions(ji);
            execs.sort(
                    Comparator.comparing(
                            JobExecution::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed());
            for (JobExecution je : execs) {
                sb.append(row(je)).append('\n');
            }
        }
        return sb.toString();
    }

    @ShellMethod(key = "last", value = "Показать последнюю попытку выполнения джоба")
    public String last(@ShellOption(help = "Имя джоба") String jobName) {
        JobInstance last = jobExplorer.getLastJobInstance(jobName);
        if (last == null) {
            return "Нет запусков для: " + jobName;
        }
        List<JobExecution> execs = jobExplorer.getJobExecutions(last);
        execs.sort(
                Comparator.comparing(JobExecution::getCreateTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed());
        return execs.isEmpty() ? "Нет execution’ов для: " + jobName : row(execs.get(0));
    }

    @ShellMethod(key = "status", value = "Показать статус executionId")
    public String status(@ShellOption(help = "Job Execution Id") long executionId) {
        JobExecution je = jobExplorer.getJobExecution(executionId);
        if (je == null) {
            return "Execution не найден: " + executionId;
        }
        return row(je) + "\nSteps:\n" + steps(je);
    }

    private String row(JobExecution je) {
        return String.format(Locale.ROOT,
                "execId=%d, job=%s, status=%s, exit=%s, started=%s, ended=%s, params=%s",
                je.getId(),
                je.getJobInstance() != null ? je.getJobInstance().getJobName() : "?",
                je.getStatus(),
                je.getExitStatus(),
                ts(je.getStartTime()),
                ts(je.getEndTime()),
                jobOperatorSafeParams(je.getId()));
    }

    private String steps(JobExecution je) {
        StringBuilder sb = new StringBuilder();
        for (StepExecution se : je.getStepExecutions()) {
            sb.append(
                    String.format(Locale.ROOT,
                            "  - step=%s, " +
                                    "status=%s, " +
                                    "read=%d, write=%d, " +
                                    "filter=%d, commit=%d, " +
                                    "skip(r/p/w)=%d/%d/%d, " +
                                    "started=%s, ended=%s%n",
                    se.getStepName(), se.getStatus(),
                    se.getReadCount(), se.getWriteCount(), se.getFilterCount(), se.getCommitCount(),
                    se.getReadSkipCount(), se.getProcessSkipCount(), se.getWriteSkipCount(),
                    ts(se.getStartTime()), ts(se.getEndTime())));
        }
        return sb.toString();
    }

    private String ts(LocalDateTime t) {
        return t == null ? "-" : t.atZone(ZoneId.systemDefault()).format(TS);
    }

    private String jobOperatorSafeParams(Long executionId) {
        try {
            return jobOperator.getParameters(executionId);
        } catch (Exception e) {
            return "<n/a>";
        }
    }

    @ShellMethod(key = "help migration", value = "Подсказка по параметрам миграции")
    public String help() {
        return """
                Параметры (defaults из application.yml):
                  - migration.chunk-size       = %d
                  - migration.split-threads    = %d
                  - migration.truncate-before-load = %s
                  - migration.skip-limit       = %d

                Команды:
                  • run pg2mongo  --truncate [true|false] [--since 2024-01-01T00:00:00Z]
                  • run mongo2pg  --truncate [true|false] [--since 2024-01-01T00:00:00Z]
                  • jobs
                  • executions --jobName <name> [--limit 10]
                  • last --jobName <name>
                  • status --executionId <id>
                  • stop --executionId <id>
                  • restart --executionId <id> [--params "k=v,k2=v2"]
                """.formatted(
                props.getChunkSize(),
                props.getSplitThreads(),
                props.isTruncateBeforeLoad(),
                props.getSkipLimit()
        );
    }
}

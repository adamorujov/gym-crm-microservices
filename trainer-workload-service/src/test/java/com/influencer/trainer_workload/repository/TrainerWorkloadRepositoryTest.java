package com.influencer.trainer_workload.repository;

import com.influencer.trainer_workload.model.TrainerTrainingSummary;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
class TrainerWorkloadRepositoryTest {

    @Autowired
    private TrainerWorkloadRepository repository;

    @AfterEach
    void cleanUp() {
        repository.deleteAll();
    }

    @Test
    void save_andFindByTrainerUsername_returnsSavedDocument() {
        TrainerTrainingSummary summary = new TrainerTrainingSummary(
                "jane.smith", "Jane", "Smith", true);
        summary.setYears(new ArrayList<>());

        repository.save(summary);

        Optional<TrainerTrainingSummary> found = repository.findByTrainerUsername("jane.smith");
        assertThat(found).isPresent();
        assertThat(found.get().getTrainerFirstName()).isEqualTo("Jane");
        assertThat(found.get().getTrainerLastName()).isEqualTo("Smith");
        assertThat(found.get().getTrainerStatus()).isTrue();
    }

    @Test
    void findByTrainerUsername_notFound_returnsEmpty() {
        Optional<TrainerTrainingSummary> found = repository.findByTrainerUsername("nobody");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByTrainerUsername_existingRecord_returnsTrue() {
        repository.save(new TrainerTrainingSummary("bob.jones", "Bob", "Jones", false));
        assertThat(repository.existsByTrainerUsername("bob.jones")).isTrue();
    }

    @Test
    void existsByTrainerUsername_missingRecord_returnsFalse() {
        assertThat(repository.existsByTrainerUsername("ghost")).isFalse();
    }

    @Test
    void save_updatesExistingDocument_whenCalledTwice() {
        TrainerTrainingSummary summary = new TrainerTrainingSummary("alice", "Alice", "Wonder", true);
        summary.setYears(new ArrayList<>());

        TrainerTrainingSummary.YearSummary ys = new TrainerTrainingSummary.YearSummary(2024);
        ys.setMonths(new ArrayList<>());
        ys.getMonths().add(new TrainerTrainingSummary.MonthSummary(4, 60));
        summary.getYears().add(ys);
        repository.save(summary);

        // update
        TrainerTrainingSummary loaded = repository.findByTrainerUsername("alice").get();
        loaded.getYears().get(0).getMonths().get(0).setTrainingsSummaryDuration(90);
        repository.save(loaded);

        TrainerTrainingSummary updated = repository.findByTrainerUsername("alice").get();
        assertThat(updated.getYears().get(0).getMonths().get(0).getTrainingsSummaryDuration()).isEqualTo(90);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void findAll_returnsAllDocuments() {
        repository.save(new TrainerTrainingSummary("t1", "First1", "Last1", true));
        repository.save(new TrainerTrainingSummary("t2", "First2", "Last2", false));

        assertThat(repository.findAll()).hasSize(2);
    }
}

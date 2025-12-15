package fr.esgi.service.impl;

import fr.esgi.service.SchedulerServiceImpl;
import org.junit.jupiter.api.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SchedulerServiceImpl")
class SchedulerServiceImplTest {

    private SchedulerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SchedulerServiceImpl();
    }

    @AfterEach
    void tearDown() {
        service.shutdown();
    }

    @Nested
    @DisplayName("schedule()")
    class Schedule {

        @Test
        @DisplayName("devrait exécuter la tâche après le délai")
        void devrait_executer_apres_delai() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            service.schedule(() -> {
                executed.set(true);
                latch.countDown();
            }, 50);

            assertThat(executed.get()).isFalse();

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

            assertThat(completed).isTrue();
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("devrait pouvoir annuler une tâche planifiée")
        void devrait_annuler_tache() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            ScheduledFuture<?> future = service.schedule(() -> executed.set(true), 200);
            future.cancel(false);

            Thread.sleep(300);

            assertThat(executed.get()).isFalse();
        }

        @Test
        @DisplayName("devrait exécuter plusieurs tâches")
        void devrait_executer_plusieurs_taches() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            service.schedule(() -> { counter.incrementAndGet(); latch.countDown(); }, 10);
            service.schedule(() -> { counter.incrementAndGet(); latch.countDown(); }, 20);
            service.schedule(() -> { counter.incrementAndGet(); latch.countDown(); }, 30);

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

            assertThat(completed).isTrue();
            assertThat(counter.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("devrait fonctionner avec délai 0")
        void devrait_fonctionner_delai_zero() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            service.schedule(() -> {
                executed.set(true);
                latch.countDown();
            }, 0);

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

            assertThat(completed).isTrue();
            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("scheduleAtFixedRate()")
    class ScheduleAtFixedRate {

        @Test
        @DisplayName("devrait exécuter la tâche périodiquement")
        void devrait_executer_periodiquement() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(3);

            service.scheduleAtFixedRate(() -> {
                counter.incrementAndGet();
                latch.countDown();
            }, 0, 50);

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

            assertThat(completed).isTrue();
            assertThat(counter.get()).isGreaterThanOrEqualTo(3);
        }

        @Test
        @DisplayName("devrait respecter le délai initial")
        void devrait_respecter_delai_initial() throws InterruptedException {
            AtomicBoolean executed = new AtomicBoolean(false);

            service.scheduleAtFixedRate(() -> executed.set(true), 100, 50);

            Thread.sleep(50);
            assertThat(executed.get()).isFalse();

            Thread.sleep(100);
            assertThat(executed.get()).isTrue();
        }

        @Test
        @DisplayName("devrait pouvoir être annulé")
        void devrait_pouvoir_etre_annule() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);

            ScheduledFuture<?> future = service.scheduleAtFixedRate(counter::incrementAndGet, 0, 50);

            Thread.sleep(120);
            future.cancel(false);
            int countAtCancel = counter.get();

            Thread.sleep(150);

            // Le compteur ne devrait plus augmenter après annulation
            assertThat(counter.get()).isEqualTo(countAtCancel);
        }
    }

    @Nested
    @DisplayName("shutdown()")
    class Shutdown {

        @Test
        @DisplayName("devrait arrêter le scheduler")
        void devrait_arreter_scheduler() {
            service.schedule(() -> {}, 1000);

            service.shutdown();

            // Après shutdown, le service devrait se recréer automatiquement
            // grâce à ensureRunning()
        }

        @Test
        @DisplayName("devrait pouvoir être appelé plusieurs fois")
        void devrait_appeler_plusieurs_fois() {
            service.shutdown();
            service.shutdown();
            service.shutdown();
            // Ne devrait pas lever d'exception
        }

        @Test
        @DisplayName("devrait permettre de planifier après shutdown (auto-recovery)")
        void devrait_permettre_planifier_apres_shutdown() throws InterruptedException {
            service.shutdown();

            AtomicBoolean executed = new AtomicBoolean(false);
            CountDownLatch latch = new CountDownLatch(1);

            service.schedule(() -> {
                executed.set(true);
                latch.countDown();
            }, 10);

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

            assertThat(completed).isTrue();
            assertThat(executed.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("ensureRunning() (auto-recovery)")
    class EnsureRunning {

        @Test
        @DisplayName("devrait recréer le scheduler après shutdown")
        void devrait_recreer_apres_shutdown() throws InterruptedException {
            AtomicInteger counter = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(2);

            service.schedule(() -> { counter.incrementAndGet(); latch.countDown(); }, 10);
            service.shutdown();
            service.schedule(() -> { counter.incrementAndGet(); latch.countDown(); }, 10);

            boolean completed = latch.await(500, TimeUnit.MILLISECONDS);

            assertThat(completed).isTrue();
            assertThat(counter.get()).isEqualTo(2);
        }
    }
}
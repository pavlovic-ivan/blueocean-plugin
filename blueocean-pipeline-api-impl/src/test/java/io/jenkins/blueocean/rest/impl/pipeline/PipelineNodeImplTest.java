package io.jenkins.blueocean.rest.impl.pipeline;

import io.jenkins.blueocean.rest.model.BlueRun;
import io.jenkins.blueocean.service.embedded.rest.QueueUtil;
import org.jenkinsci.plugins.pipeline.modeldefinition.actions.RestartDeclarativePipelineAction;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ WorkflowRun.class, WorkflowJob.class, QueueUtil.class })
@PowerMockIgnore({"javax.crypto.*", "javax.security.*", "javax.net.ssl.*", "com.sun.org.apache.xerces.*", "com.sun.org.apache.xalan.*", "javax.xml.*", "org.xml.*", "org.w3c.dom.*"})
public class PipelineNodeImplTest {
    @Mock
    WorkflowJob job;

    @Mock
    WorkflowRun run;

    @Test
    public void getRun_NeverFound() throws Exception {
        PowerMockito.mockStatic(QueueUtil.class);
        PowerMockito.when(QueueUtil.getRun(job, 1)).thenReturn(null);

        WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
        assertNull(workflowRun);

        PowerMockito.verifyStatic(QueueUtil.class, VerificationModeFactory.atLeastOnce());
        QueueUtil.getRun(job, 1); // need to call again to handle verify
    }

    @Test
    public void getRun_FirstFound() throws Exception {
        PowerMockito.mockStatic(QueueUtil.class);
        PowerMockito.when(QueueUtil.getRun(job, 1)).thenReturn(run);

        WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
        assertEquals(workflowRun, run);

        PowerMockito.verifyStatic(QueueUtil.class, VerificationModeFactory.times(1));
        QueueUtil.getRun(job, 1); // need to call again to handle verify
    }

    @Test
    public void getRun_EventuallyFound() throws Exception {
        PowerMockito.mockStatic(QueueUtil.class);
        PowerMockito.when(QueueUtil.getRun(job, 1)).thenReturn(null).thenReturn(null).thenReturn(null).thenReturn(run);

        WorkflowRun workflowRun = PipelineNodeImpl.getRun(job, 1);
        assertEquals(workflowRun, run);

        PowerMockito.verifyStatic(QueueUtil.class, VerificationModeFactory.times(4));
        QueueUtil.getRun(job, 1); // need to call again to handle verify
    }

    @Test
    public void isFeatureRestartableStagesEnabled_allStagesDisabled(){
        System.setProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES", "true");
        assertFalse(PipelineNodeImpl.isFeatureRestartableStagesEnabled());
        System.clearProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES");
    }

    @Test
    public void isFeatureRestartableStagesEnabled_allStagesEnabled(){
        System.setProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES", "false");
        assertTrue(PipelineNodeImpl.isFeatureRestartableStagesEnabled());
        System.clearProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES");
    }

    @Test
    public void isFeatureRestartableStagesEnabled_allStagesEnabled_valueTypo(){
        System.setProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES", "typo");
        assertTrue(PipelineNodeImpl.isFeatureRestartableStagesEnabled());
        System.clearProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES");
    }

    @Test
    public void isRestartable_stagesAreNotRestartable() throws Exception {
        System.setProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES", "true");

        PipelineNodeImpl underTest = PowerMockito.mock(PipelineNodeImpl.class);
        PowerMockito.field(PipelineNodeImpl.class, "run").set(underTest, run);

        RestartDeclarativePipelineAction restartDeclarativePipelineAction = PowerMockito.mock(RestartDeclarativePipelineAction.class);
        PowerMockito.when(run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(restartDeclarativePipelineAction);
        PowerMockito.when(underTest.isRestartable()).thenCallRealMethod();

        Boolean isStageRestartable = underTest.isRestartable();
        Assert.assertNotNull(isStageRestartable);
        Assert.assertFalse(isStageRestartable);
        System.clearProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES");
    }

    @Test
    public void isRestartable_stagesAreNotRestartable_Typo() throws Exception {
        System.setProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES", "t");

        PipelineNodeImpl underTest = PowerMockito.mock(PipelineNodeImpl.class);
        PowerMockito.field(PipelineNodeImpl.class, "run").set(underTest, run);

        RestartDeclarativePipelineAction restartDeclarativePipelineAction = PowerMockito.mock(RestartDeclarativePipelineAction.class);
        PowerMockito.when(run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(restartDeclarativePipelineAction);
        PowerMockito.when(underTest.isRestartable()).thenCallRealMethod();

        Boolean isStageRestartable = underTest.isRestartable();
        Assert.assertNotNull(isStageRestartable);
        Assert.assertFalse(isStageRestartable);
        System.clearProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES");
    }

    @Test
    public void isRestartable_stagesAreRestartable() throws Exception {
        System.setProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES", "false");
        String restartableStageUnderTest = "restartableStageUnderTest";

        PipelineNodeImpl underTest = PowerMockito.mock(PipelineNodeImpl.class);
        PowerMockito.field(PipelineNodeImpl.class, "run").set(underTest, run);

        RestartDeclarativePipelineAction restartDeclarativePipelineAction = PowerMockito.mock(RestartDeclarativePipelineAction.class);
        PowerMockito.when(run.getAction(RestartDeclarativePipelineAction.class)).thenReturn(restartDeclarativePipelineAction);

        List<String> restartableStages = Arrays.asList(restartableStageUnderTest);
        PowerMockito.when(restartDeclarativePipelineAction.getRestartableStages()).thenReturn(restartableStages);
        PowerMockito.when(underTest.getDisplayName()).thenReturn(restartableStageUnderTest);
        PowerMockito.when(underTest.getStateObj()).thenReturn(BlueRun.BlueRunState.FINISHED);
        PowerMockito.when(underTest.isRestartable()).thenCallRealMethod();

        Boolean isStageRestartable = underTest.isRestartable();
        Assert.assertNotNull(isStageRestartable);
        Assert.assertTrue(isStageRestartable);
        System.clearProperty("blueocean.features.DISABLE_RESTARTABLE_STAGES");
    }
}

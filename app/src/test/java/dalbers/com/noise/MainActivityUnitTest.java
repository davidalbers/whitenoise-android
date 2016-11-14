package dalbers.com.noise;

import android.os.Bundle;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import audio.WhiteNoiseAudioInterface;

import static dalbers.com.noise.WhiteNoisePresenter.SAVE_STATE_TIMER_CREATED;
import static dalbers.com.noise.WhiteNoisePresenter.SAVE_STATE_TIMER_TIME;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.any;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by davidalbers on 10/22/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class MainActivityUnitTest {
    @Mock
    private WhiteNoiseAudioInterface whiteNoiseAudioPlayer;

    @Mock
    private WhiteNoiseContract.View view;

    private WhiteNoisePresenter presenter;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        presenter = new WhiteNoisePresenter(view,whiteNoiseAudioPlayer);
    }

    @Test
    public void playWhite() {
        presenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.white);
        presenter.playPause();
        //set file is called twice, once at initialization of the player and once when we set the
        //color explicitly. We only need to verify the second time's parameters
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        verify(whiteNoiseAudioPlayer, atLeastOnce()).setSoundFile(argument.capture());
        List<Integer> values = argument.getAllValues();
        Assert.assertEquals((int)values.get(0),R.raw.white);
        values.remove(0);
        Assert.assertEquals((int)values.get(0),R.raw.white);
    }

    @Test
    public void playPink() {
        presenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.pink);
        presenter.playPause();
        //set file is called twice, once at initialization of the player and once when we set the
        //color explicitly. We only need to verify the second time
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        verify(whiteNoiseAudioPlayer, atLeastOnce()).setSoundFile(argument.capture());
        List<Integer> values = argument.getAllValues();
        Assert.assertEquals((int)values.get(0),R.raw.white);
        values.remove(0);
        Assert.assertEquals((int)values.get(0),R.raw.pink);
    }

    @Test
    public void playBrown() {
        presenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.brown);
        presenter.playPause();
        //set file is called twice, once at initialization of the player and once when we set the
        //color explicitly. We only need to verify the second time
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);
        verify(whiteNoiseAudioPlayer, atLeastOnce()).setSoundFile(argument.capture());
        List<Integer> values = argument.getAllValues();
        Assert.assertEquals((int)values.get(0),R.raw.white);
        values.remove(0);
        Assert.assertEquals((int)values.get(0),R.raw.brown);
    }

    @Test
    public void playMultiple() {
        //Play a bunch of sound files and verify all are set correctly
        presenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.brown);
        presenter.playPause();
        presenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.white);
        presenter.setNoiseColor(WhiteNoisePresenter.NoiseColors.pink);
        ArgumentCaptor<Integer> argument = ArgumentCaptor.forClass(Integer.class);

        verify(whiteNoiseAudioPlayer, atLeastOnce()).setSoundFile(argument.capture());

        List<Integer> values = argument.getAllValues();

        Assert.assertEquals((int)values.get(0),R.raw.white);
        values.remove(0);
        Assert.assertEquals((int)values.get(0),R.raw.brown);
        values.remove(0);
        Assert.assertEquals((int)values.get(0),R.raw.white);
        values.remove(0);
        Assert.assertEquals((int)values.get(0),R.raw.pink);

    }

    @Test
    public void setVolume() {
        presenter.setVolume(.77f);
        verify(whiteNoiseAudioPlayer).setMaxVolume(.77f);
    }


    @Test
    public void fadeAndOscillate() {
        presenter.toggleFade(true);
        verify(whiteNoiseAudioPlayer).setDecreaseVolume(true);
        presenter.toggleOscillation(true);
        verify(whiteNoiseAudioPlayer).setOscillateVolume(true);
    }

    @Test
    public void createTimer() {
        presenter.createCancelTimer();
        verify(view).showPickerDialog();
    }

    @Test
    public void cancelTimer() {
        presenter.createCancelTimer();
        presenter.createCancelTimer();
        verify(view).setTime(0);
        verify(view).setTimerUIUnsetState();
        verify(view).setPlayButtonPlay();
    }

}

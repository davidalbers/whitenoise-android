package dalbers.com.noise;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import audio.FakeAudioService;
import audio.WhiteNoiseAudioInterface;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.verify;

/**
 * Created by davidalbers on 11/14/16.
 * Do any tests that require media service with state, i.e. stubbed not mocked
 */

@RunWith(MockitoJUnitRunner.class)
public class PresenterMediaInteractionTest {
        private WhiteNoiseAudioInterface whiteNoiseAudioPlayer;

        @Mock
        private WhiteNoiseContract.View view;

        private WhiteNoisePresenter presenter;

        @Before
        public void setup() {
            MockitoAnnotations.initMocks(this);
            whiteNoiseAudioPlayer = new FakeAudioService();
            presenter = new WhiteNoisePresenter(view,whiteNoiseAudioPlayer);
        }

        @Test
        public void playPause() {
            presenter.playPause();
            presenter.playPause();
            InOrder inOrder = Mockito.inOrder(view);
            inOrder.verify(view).setPlayButtonPlay();
            inOrder.verify(view).setPlayButtonPause();
        }

        @Test
        public void timerSetWhilePlaying() {
            presenter.playPause();
            presenter.setTime(1000l);
            verify(view).setPlayButtonPlay();
            assertEquals(whiteNoiseAudioPlayer.getTimeLeft(),1000l);
        }

}

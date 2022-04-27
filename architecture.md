# Architecture

## Intro

A few things influnce this project's architecture:

* its UI is written 100% in Jetpack Compose. While Compose is a UI toolkit, it comes with some new ways to approach architecture laid out by [Google](https://developer.android.com/jetpack/compose/mental-model).
* it uses a [Service](https://github.com/davidalbers/whitenoise-android/blob/master/app/src/main/java/dalbers/com/noise/service/AudioPlayerService.kt) for playing Audio in the background. Services behave slightly differently than Activities and Fragments and therefore behave have different requirements for architecture.
* its code follows the model-view-viewmodel (MVVM) pattern, particularly the flavor of MVVM [outlined by Google](https://developer.android.com/jetpack/guide)

### Jetpack Compose

The biggest architectural influence Compose has is the idea of [state-hoisting](https://developer.android.com/jetpack/compose/state#state-hoisting) and stateless Composables. If you look at the `Player` [Composable](https://github.com/davidalbers/whitenoise-android/blob/master/app/src/main/java/dalbers/com/noise/playerscreen/view/Player.kt), it's completely state-less. In other words the state is being managed at higher level, in the [ViewModel](https://github.com/davidalbers/whitenoise-android/blob/d4946b7fbced96cbcd1259154667a4e3982074b2/app/src/main/java/dalbers/com/noise/playerscreen/view/PlayerScreen.kt#L35).

### Service

#### Is it a View?

The [`AudioPlayerService`](https://github.com/davidalbers/whitenoise-android/blob/master/app/src/main/java/dalbers/com/noise/service/AudioPlayerService.kt) is kind of a View and kind of not. It shows a Notification that allows the user to play and pause audio. However, it's also playing the audio in the background and would be required even if we didn't show a notification. I tried to separate these the best I could by creating [`AudioController`](https://github.com/davidalbers/whitenoise-android/blob/8bc22b55580ccd10ca918110dfd10f5800837a7a/app/src/main/java/dalbers/com/noise/audiocontrol/AudioController.kt). The service is really just a "holder" for the audio controller.

#### Does it need a ViewModel?

Given that the service is kind-of-a-View, it needs a ViewModel to handle the logic for showing managing the view state. I created [`AudioPlayerViewModel`](https://github.com/davidalbers/whitenoise-android/blob/8bc22b5550ccd10ca918110dfd10f5800837a7a/app/src/main/java/dalbers/com/noise/service/viewmodel/AudioPlayerViewModel.kt) which is an Androidx `ViewModel`. A service doesn't have the same lifecycle as an Activity and there's no `by viewModels` so I'm not completely convinced that I needed a VM here though. 

#### Service and Activity connection

The Service creates `AudioController` because the Context needs to be the Service's `Context`. [`MainActivity`](https://github.com/davidalbers/whitenoise-android/blob/8bc22b55580ccd10ca918110dfd10f5800837a7a/app/src/main/java/dalbers/com/noise/shared/MainActivity.kt) starts and binds to the Service which involves getting a reference to `AudioController`. I felt this kept the best separation-of-concerns since each piece has a particular job. This means that the [`PlayerScreenViewModel`](https://github.com/davidalbers/whitenoise-android/blob/8bc22b55580ccd10ca918110dfd10f5800837a7a/app/src/main/java/dalbers/com/noise/playerscreen/viewmodel/PlayerScreenViewModel.kt) has a nullable `AudioController` and dependency injection will be more complicated. This part could be iterated on too.

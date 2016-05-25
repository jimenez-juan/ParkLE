package edu.stanford.parkle;

import android.content.Context;
import android.widget.ImageView;

import com.getkeepsafe.android.multistateanimation.MultiStateAnimation;

/**
 * Created by Lizzie on 5/12/2016.
 */
public class CarAnimation {
    private Context context;
    private ImageView view;
    private MultiStateAnimation animation;

    public CarAnimation(Context context, ImageView view){
        this.context = context;
        this.view = view;
        createAnimation();
    }

    private void createAnimation(){
        MultiStateAnimation.SectionBuilder firstSection = new MultiStateAnimation.SectionBuilder("first_section")
                // If true, this section will play once and stop. Otherwise it
                // will loop indefinitely.
                .setOneshot(false)
                        // The number of milliseconds that each frame of this section will
                        // play
                .setFrameDuration(100)
                        // Each frame is the name of an image resource. They will be
                        // played in the order added.
                .addFrame(R.drawable.logo2_3)
                .addFrame(R.drawable.logo2_2)
                .addFrame(R.drawable.logo2_1)

                .addFrame(R.drawable.logo2)
                .addFrame(R.drawable.logo)
                .addFrame(R.drawable.logo3)
                .addFrame(R.drawable.logo4)
                .addFrame(R.drawable.logo2)

                .addFrame(R.drawable.logo2_1)
                .addFrame(R.drawable.logo2_2)
                .addFrame(R.drawable.logo2_3)
                ;

        // The frames of a transition will be played before playing
        // the normal frames of this section when transitioning. In
        // this case, the frames for this transition will play if
        // "first_section" is playing when queueTransition("second_section")
        // is called
        MultiStateAnimation.TransitionBuilder transitionFromFirst = new MultiStateAnimation.TransitionBuilder()
                .setFrameDuration(33)
                .addFrame(R.drawable.logo)
                .addFrame(R.drawable.logo2)
                .addFrame(R.drawable.logo4);

        // As a special case, a transition ID of "" is a transition
        // from nothing. It will play if the associated section is the
        // first to ever play.
        MultiStateAnimation.TransitionBuilder transitionFromNothing = new MultiStateAnimation.TransitionBuilder()
                .addFrame(R.drawable.logo2)
                .addFrame(R.drawable.logo4);

        // A section with a single frame and "oneshot" set to true is
        // equivalent to a static image
        MultiStateAnimation.SectionBuilder secondSection = new MultiStateAnimation.SectionBuilder("second_section")
                .setOneshot(true)
                .addTransition("first_section", transitionFromFirst)
                .addTransition("", transitionFromNothing)
                .addFrame(R.drawable.logo);

        // Animation should be given an View that will be used to display the animation.
        animation = new MultiStateAnimation.Builder(view)
                .addSection(firstSection)
                        //.addSection(secondSection)
                .build(context);
    }

    public void Animate(){
        animation.queueTransition("first_section");
    }


}

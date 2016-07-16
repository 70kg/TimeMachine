package me.drakeet.timemachine;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * Non-UI Fragment, to be a helper of its parent.
 *
 * @author drakeet
 */
public class CoreHelper extends Fragment {

    private static final String FRAG_TAG = CoreHelper.class.getCanonicalName();


    public interface CoreFragment {
        // TODO: 16/5/22
    }


    public static <ParentFragment extends Fragment & CoreFragment> CoreHelper attach(
        @Nullable ParentFragment parent) {
        return attach(parent.getChildFragmentManager());
    }


    public static <ParentActivity extends FragmentActivity & CoreFragment> CoreHelper attach(
        @Nullable ParentActivity parent) {
        return attach(parent.getSupportFragmentManager());
    }


    private static CoreHelper attach(@Nullable FragmentManager fragmentManager) {
        CoreHelper helper = (CoreHelper) fragmentManager.findFragmentByTag(FRAG_TAG);
        if (helper == null) {
            helper = new CoreHelper();
            fragmentManager.beginTransaction().add(helper, FRAG_TAG).commit();
        }
        return helper;
    }


    @Nullable private CoreFragment getParent() {
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CoreFragment) {
            return (CoreFragment) parentFragment;
        } else {
            Activity activity = getActivity();
            if (activity instanceof CoreFragment) {
                return (CoreFragment) activity;
            }
        }
        return null;
    }
}
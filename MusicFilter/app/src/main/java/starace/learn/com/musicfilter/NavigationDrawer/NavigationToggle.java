package starace.learn.com.musicfilter.NavigationDrawer;

/**
 * Created by mstarace on 5/2/16.
 */
public class NavigationToggle extends NaviagtionEntry {

    private String title;
    private boolean checked;

    public NavigationToggle(String title) {
        this.setTitle(title);
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }

}

package starace.learn.com.musicfilter.NavigationDrawer.Models;

/**
 * Created by mstarace on 5/2/16.
 */
public class NavigationItem extends NavigationEntry {
    private String title;

    public NavigationItem(String title) {
        this.setTitle(title);
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }
}

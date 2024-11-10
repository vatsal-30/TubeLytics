package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Utsav Patel
 */
public class SearchFormModelTest {
    private SearchForm searchForm;

    /**
     * This method sets up a dummy SearchForm object to test its constructors, getters, and setters.
     *
     * @author Utsav Patel
     */
    @Before
    public void setup() {
        searchForm = new SearchForm();
    }

    /**
     * This method tests SearchForm object.
     *
     * @author Utsav Patel
     */
    @Test
    public void searchFormTest() {
        searchForm.setQuery("java");
        Assert.assertEquals("java", searchForm.getQuery());
    }
}

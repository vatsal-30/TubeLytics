package model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SearchFormModelTest {
    private SearchForm searchForm;
    @Before
    public void setup() {
        searchForm = new SearchForm();
    }

    @Test
    public void searchFormTest() {
        searchForm.setQuery("java");
        Assert.assertEquals("java", searchForm.getQuery());
    }
}

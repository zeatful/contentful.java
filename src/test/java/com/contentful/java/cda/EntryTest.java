package com.contentful.java.cda;

import com.contentful.java.cda.lib.Enqueue;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class EntryTest extends BaseTest {
  @Test
  @Enqueue("demo/entries_nyancat.json")
  public void fetchEntry() throws Exception {
    assertNyanCat(client.fetch(CDAEntry.class).one("nyancat"));
  }

  @Test
  @Enqueue("demo/entries_nyancat.json")
  public void fetchEntryAsync() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1);
    final CDAEntry[] result = { null };

    client.fetch(CDAEntry.class).one("nyancat", new CDACallback<CDAEntry>() {
      @Override protected void onSuccess(CDAEntry entry) {
        result[0] = entry;
        latch.countDown();
      }

      @Override protected void onFailure(Throwable error) {
        latch.countDown();
      }
    });

    latch.await();
    assertThat(result[0]).isNotNull();
    assertNyanCat(result[0]);
  }

  @Test
  @Enqueue("demo/entries.json")
  public void fetchAllEntries() throws Exception {
    CDAArray array = client.fetch(CDAEntry.class).all();
    assertThat(array.items()).hasSize(15);
    assertThat(array.assets()).hasSize(4);
    assertThat(array.entries()).hasSize(11);

    assertThat(array.total()).isEqualTo(11);
    assertThat(array.skip()).isEqualTo(0);
    assertThat(array.limit()).isEqualTo(100);

    CDAEntry nyanCat = array.entries().get("nyancat");
    assertThat(nyanCat).isNotNull();
    assertNyanCat(nyanCat);
  }

  private void assertNyanCat(CDAEntry entry) {
    assertThat(entry.id()).isEqualTo("nyancat");
    assertThat(entry.getField("name")).isEqualTo("Nyan Cat");
    assertThat(entry.getField("color")).isEqualTo("rainbow");
    assertThat(entry.getField("birthday")).isEqualTo("2011-04-04T22:00:00+00:00");
    assertThat(entry.getField("lives")).isEqualTo(1337.0);

    List<String> likes = entry.getField("likes");
    assertThat(likes).containsExactly("rainbows", "fish");

    Object bestFriend = entry.getField("bestFriend");
    assertThat(bestFriend).isInstanceOf(CDAEntry.class);
    assertThat(entry).isSameAs(((CDAEntry) bestFriend).getField("bestFriend"));
  }
}
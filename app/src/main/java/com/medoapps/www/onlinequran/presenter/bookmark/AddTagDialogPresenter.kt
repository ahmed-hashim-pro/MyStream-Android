package com.medoapps.www.onlinequran.presenter.bookmark

import com.quran.data.model.bookmark.Tag
import com.medoapps.www.onlinequran.model.bookmark.BookmarkModel
import com.medoapps.www.onlinequran.presenter.Presenter
import com.medoapps.www.onlinequran.ui.fragment.AddTagDialog

import javax.inject.Inject

class AddTagDialogPresenter @Inject
internal constructor(private val bookmarkModel: BookmarkModel) : Presenter<AddTagDialog> {
  private var dialog: AddTagDialog? = null
  private var tags: List<Tag> = emptyList()

  init {
    bookmarkModel.tagsObservable
        .subscribe { it -> this.tags = it }
  }

  fun validate(tagName: String, tagId: Long): Boolean {
    if (tagName.isBlank()) {
      dialog?.onBlankTagName()
      return false
    } else {
      if (tags.any { it.name == tagName && it.id != tagId }) {
        dialog?.onDuplicateTagName()
        return false
      }
    }
    return true
  }

  fun addTag(tagName: String) {
    bookmarkModel.addTagObservable(tagName)
        .subscribe()
  }

  fun updateTag(tag: Tag) {
    bookmarkModel.updateTag(tag)
        .subscribe()
  }

  override fun bind(dialog: AddTagDialog) {
    this.dialog = dialog
  }

  override fun unbind(dialog: AddTagDialog) {
    if (this.dialog === dialog) {
      this.dialog = null
    }
  }
}

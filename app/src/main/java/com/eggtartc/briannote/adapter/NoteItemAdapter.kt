package com.eggtartc.briannote.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.eggtartc.briannote.databinding.ItemHeaderBinding
import com.eggtartc.briannote.databinding.ItemNoteBinding
import com.eggtartc.briannote.extension.format
import com.eggtartc.briannote.interfaces.INoteItem
import com.eggtartc.briannote.model.Note
import com.eggtartc.briannote.model.NoteHeader
import com.eggtartc.briannote.util.HtmlUtils
import com.google.android.material.elevation.SurfaceColors
import java.text.DateFormat

class NoteItemAdapter<ActivityType>(
    private val activity: ActivityType,
    private val notes: List<INoteItem>
) : Adapter<NoteItemAdapter.BaseViewHolder>()
        where ActivityType : Activity,
              ActivityType : NoteItemAdapter.OnNoteItemClickListener {
    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_NOTE = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        if (viewType == VIEW_TYPE_NOTE) {
            return NoteViewHolder(
                ItemNoteBinding.inflate(activity.layoutInflater, parent, false))
        }
        return HeaderViewHolder(
            ItemHeaderBinding.inflate(activity.layoutInflater, parent, false))
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (notes[position].isHeader()) VIEW_TYPE_HEADER else VIEW_TYPE_NOTE
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder.itemViewType) {
            VIEW_TYPE_HEADER -> bindHeaderViewHolder(holder as HeaderViewHolder, position)
            VIEW_TYPE_NOTE -> bindNoteViewHolder(holder as NoteViewHolder, position)
        }
    }

    private fun bindHeaderViewHolder(holder: HeaderViewHolder, position: Int) {
        if (position >= notes.size) {
            throw IndexOutOfBoundsException(
                "position=$position is out of bounds(size=${notes.size})")
        }
        val header = notes[position] as NoteHeader
        holder.binding.apply {
            textViewHeader.text = header.title
        }
    }

    private fun bindNoteViewHolder(holder: NoteViewHolder, position: Int) {
        if (position >= notes.size) {
            throw IndexOutOfBoundsException(
                "position=$position is out of bounds(size=${notes.size})")
        }
        val note = notes[position] as Note
        val textContent = HtmlUtils.extractVisibleTextContent(note.htmlContent)

        holder.binding.apply {
            textViewTitle.text = note.title
            textViewContentPreview.text = textContent
            textViewDate.text = note.updatedAt.format("yy/MM/dd HH:mm")
            cardViewNoteItem.setCardBackgroundColor(SurfaceColors.SURFACE_2.getColor(activity))
            cardViewNoteItem.setOnClickListener { activity.onNoteItemClicked(note, cardViewNoteItem) }
            cardViewNoteItem.setOnLongClickListener { activity.onNoteItemLongClicked(note, cardViewNoteItem) }

            if (note.pinned) {
                cardViewPinningStatus.visibility = ViewGroup.VISIBLE
            }
            else {
                cardViewPinningStatus.visibility = ViewGroup.GONE
            }
        }
    }

    open class BaseViewHolder(view: View) : ViewHolder(view)

    class NoteViewHolder(val binding: ItemNoteBinding) : BaseViewHolder(binding.root)

    class HeaderViewHolder(val binding: ItemHeaderBinding) : BaseViewHolder(binding.root)

    interface OnNoteItemClickListener {
        fun onNoteItemClicked(note: Note, view: View)

        /**
         * @return true if the callback consumed the long click, false otherwise
         */
        fun onNoteItemLongClicked(note: Note, view: View): Boolean
    }
}

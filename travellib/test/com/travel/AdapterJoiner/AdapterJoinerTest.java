package com.travel.AdapterJoiner;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.travel.lib.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Created by ldkxingzhe on 2017/1/5.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19)
public class AdapterJoinerTest {
    private AdapterJoiner mAdapterJoiner;

    @Before
    public void setUp(){
        mAdapterJoiner = new AdapterJoiner();
    }
    @Test
    public void onStructureChangedTest() throws Exception{
        mAdapterJoiner.mLastGenerateTypeId = 10;
        AdapterJoiner.Joinable joinable = new JoinableStatic(10);
        mAdapterJoiner.mJoinableList.add(joinable);
        mAdapterJoiner.onStructureChanged(joinable);
        assertSame(joinable, mAdapterJoiner.mJoinedTypeToJoinable.get(10));
        assertSame(joinable, mAdapterJoiner.mJoinedTypeToJoinable.get(19));
        assertNull(null, mAdapterJoiner.mJoinedTypeToJoinable.get(20));
        assertEquals(0, mAdapterJoiner.mJoinedTypeToRealType.get(10));
        assertEquals(9, mAdapterJoiner.mJoinedTypeToRealType.get(19));
        assertEquals(10, mAdapterJoiner.mJoinableToRealTypeToJoinedType.get(joinable).get(0));
        assertEquals(19, mAdapterJoiner.mJoinableToRealTypeToJoinedType.get(joinable).get(9));
    }

    @Test
    public void onDataSetChangedTest() throws Exception{
        JoinableStatic joinable1 = new JoinableStatic(10);
        JoinableStatic joinable2 = new JoinableStatic(5);
        JoinableStatic joinable3 = new JoinableStatic(3);
        mAdapterJoiner.mJoinableList.add(joinable1);
        mAdapterJoiner.mJoinableList.add(joinable2);
        mAdapterJoiner.mJoinableList.add(joinable3);
        mAdapterJoiner.onStructureChanged(joinable1);
        mAdapterJoiner.onStructureChanged(joinable2);
        mAdapterJoiner.onStructureChanged(joinable3);
        mAdapterJoiner.onDataSetChanged();

        assertSame(joinable1, mAdapterJoiner.mJoinedPosToJoinable.get(0));
        assertSame(joinable1, mAdapterJoiner.mJoinedPosToJoinable.get(9));
        assertSame(joinable3, mAdapterJoiner.mJoinedPosToJoinable.get(20));
        assertSame(joinable3, mAdapterJoiner.mJoinedPosToJoinable.get(29));

        assertEquals(0, (int)mAdapterJoiner.mJoinedPosToJoinType.get(0));
        assertEquals(9, (int)mAdapterJoiner.mJoinedPosToJoinType.get(9));
        assertEquals(15, (int)mAdapterJoiner.mJoinedPosToJoinType.get(20));
        assertEquals(17, (int)mAdapterJoiner.mJoinedPosToJoinType.get(29));

        assertEquals(0, (int)mAdapterJoiner.mJoinedPosToRealPos.get(0));
        assertEquals(9, (int)mAdapterJoiner.mJoinedPosToRealPos.get(9));
        assertEquals(0, (int)mAdapterJoiner.mJoinedPosToRealPos.get(20));
        assertEquals(9, (int)mAdapterJoiner.mJoinedPosToRealPos.get(29));

        assertEquals(30, mAdapterJoiner.mCurrentItemCount);
    }

    private static class JoinableStatic implements AdapterJoiner.Joinable{
        private int typeCount;
        public JoinableStatic(int typeCount){
            this.typeCount = typeCount;
        };

        @Override
        public RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
            return new RecyclerView.Adapter(){

                @Override
                public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    return null;
                }

                @Override
                public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

                }

                @Override
                public int getItemViewType(int position) {
                    return position >= typeCount ? typeCount - 1 : position;
                }

                @Override
                public int getItemCount() {
                    return 10;
                }
            };
        }

        @Override
        public int getTypeCount() {
            return typeCount;
        }

        @Override
        public int getTypeByIndex(int typeIndex) {
            return typeIndex;
        }
    }
}
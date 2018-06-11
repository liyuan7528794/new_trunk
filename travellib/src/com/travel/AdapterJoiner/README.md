可以实现RecyclerView拼接的工具类

以下是具体用法：
   ``` java
   mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.rv_test);
   		mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
   		AdapterJoiner joiner = new AdapterJoiner();
   		joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
   			@Override
   			public View onNeedLayout(Context context) {
   				TextView textView = new TextView(context);
   				textView.setText("哈哈");
   				textView.setBackgroundColor(Color.BLUE);
   				return textView;
   			}
   		}));
   		joiner.add(new JoinableAdapter(new MAdapter()));
   		joiner.add(new JoinableLayout(new JoinableLayout.OnNeedLayoutCallback() {
   			@Override
   			public View onNeedLayout(Context context) {
   				TextView textView = new TextView(context);
   				textView.setText("gOOD");
   				textView.setBackgroundColor(Color.RED);
   				return textView;
   			}
   		}));
   		joiner.add(new JoinableAdapter(new MAdapter()));
   		mRecyclerView.setAdapter(joiner.getAdapter());
   ```
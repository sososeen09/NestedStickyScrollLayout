# NestedStickyScrollLayout

a nested sticky scroll layout，can be a parent view of RecyclerView


# Getting started

Step 1 Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Step 2 Add the dependency
```
dependencies {
    implementation 'com.github.sososeen09:NestedStickyScrollLayout:0.0.1'
}
```

# How to use
可以像LinearLayout那样在xml中使用，不同之处在于NestedStickyScrollLayout需要设置滚动源和粘性头部
对应的属性是

|属性名|作用|说明|
|-|-|-|
|scroll_source_view|指定滚动源的id|一般来说滚动源是RecyclerView或者其父ViewGroup|
|sticky_view|指定粘性布局的id|~|

```
<com.sososeen09.sticky.NestedStickyScrollLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/activity_nest_scroll_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:scroll_source_view="@+id/fl_source"
    app:sticky_view="@+id/stick_view">

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:background="#f00"
            android:gravity="center"
            android:text="可移动上部"/>
    </FrameLayout>

    <FrameLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:clickable="true"
        android:onClick="clickView">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="40dp"
            android:background="#ff0"
            android:gravity="center"
            android:text="可移动上部2"/>
    </FrameLayout>

    <FrameLayout
        android:id="@id/stick_view"
        android:layout_width="match_parent"
        android:layout_height="50dp"
        android:background="#0ff">

        <TextView
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:gravity="center"
            android:text="粘性头部"/>
    </FrameLayout>

    <FrameLayout
        android:id="@id/fl_source"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

        <android.support.v7.widget.RecyclerView
            android:id="@+id/rv_behavior"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
    </FrameLayout>
</com.sososeen09.sticky.NestedStickyScrollLayout>
```


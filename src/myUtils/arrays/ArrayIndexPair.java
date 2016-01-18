package myUtils.arrays;

/**
 * Created by found on 1/18/16.
 */
public class ArrayIndexPair <T extends Comparable<T>> implements Comparable<T>  {
    private T item = null;
    private int index = -1;
    public ArrayIndexPair(T item,int index){
        this.index = index;
        this.item = item;
    }

    @Override
    public int compareTo(T t) {
        return item.compareTo(t);
    }
    public int getIndex(){
        return index;
    }

}

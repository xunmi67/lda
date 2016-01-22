package myUtils.myArray;

/**
 * Created by found on 1/18/16.
 */
public class ArrayIndexPair <T extends Comparable<T>> implements Comparable< ArrayIndexPair <T>>  {
    private T item = null;
    private int index = -1;
    public ArrayIndexPair(T item,int index){
        this.index = index;
        this.item = item;
    }

    @Override
    public int compareTo(ArrayIndexPair<T> t) {
        return item.compareTo(t.item);
    }
    public int getIndex(){
        return index;
    }
    public T getItem(){
        return this.item;
    }

}
